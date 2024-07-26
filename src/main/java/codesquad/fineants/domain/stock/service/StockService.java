package codesquad.fineants.domain.stock.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.dividend.service.StockDividendService;
import codesquad.fineants.domain.kis.repository.ClosingPriceRepository;
import codesquad.fineants.domain.kis.repository.CurrentPriceRepository;
import codesquad.fineants.domain.kis.service.KisService;
import codesquad.fineants.domain.stock.domain.dto.request.StockSearchRequest;
import codesquad.fineants.domain.stock.domain.dto.response.StockDataResponse;
import codesquad.fineants.domain.stock.domain.dto.response.StockRefreshResponse;
import codesquad.fineants.domain.stock.domain.dto.response.StockResponse;
import codesquad.fineants.domain.stock.domain.dto.response.StockSearchItem;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockQueryRepository;
import codesquad.fineants.domain.stock.repository.StockRepository;
import codesquad.fineants.global.errors.errorcode.StockErrorCode;
import codesquad.fineants.global.errors.exception.NotFoundResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class StockService {
	private final StockRepository stockRepository;
	private final CurrentPriceRepository currentPriceRepository;
	private final ClosingPriceRepository closingPriceRepository;
	private final StockDividendService stockDividendService;
	private final StockQueryRepository stockQueryRepository;
	private final KisService kisService;

	@Transactional(readOnly = true)
	public List<StockSearchItem> search(StockSearchRequest request) {
		return stockRepository.search(request.getSearchTerm())
			.stream()
			.map(StockSearchItem::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<StockSearchItem> search(String tickerSymbol, int size, String keyword) {
		return stockQueryRepository.getSliceOfStock(tickerSymbol, size, keyword).stream()
			.map(StockSearchItem::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public StockResponse getStock(String tickerSymbol) {
		Stock stock = stockRepository.findByTickerSymbol(tickerSymbol)
			.orElseThrow(() -> new NotFoundResourceException(StockErrorCode.NOT_FOUND_STOCK));
		return StockResponse.of(stock, currentPriceRepository, closingPriceRepository);
	}

	@Scheduled(cron = "0 0 8 * * ?") // 매일 오전 8시 (초, 분, 시간)
	@Transactional
	public void scheduledReloadStocks() {
		StockRefreshResponse response = this.reloadStocks();
		log.info("refreshStocks response : {}", response);
		stockDividendService.reloadStockDividend();
		stockDividendService.writeDividendCsvToS3();
	}

	// 상장된 종목을 한국투자증권 서버로부터 조회하고 저장한다
	@Transactional
	public StockRefreshResponse reloadStocks() {
		// 상장된 종목 조회
		List<Stock> stocks = kisService.fetchStockInfoInRangedIpo().stream()
			.map(StockDataResponse.StockIntegrationInfo::toEntity)
			.toList();
		stocks.forEach(stock -> log.info("newlyAdded Stock : {}", stock));

		// 상장된 종목 저장
		List<String> newlyAddedTickerSymbols = stockRepository.saveAll(stocks).stream()
			.map(Stock::getTickerSymbol)
			.toList();
		newlyAddedTickerSymbols.forEach(ticker -> log.info("Save the item in the database and ticker : {}", ticker));
		return StockRefreshResponse.create(newlyAddedTickerSymbols);
	}
}
