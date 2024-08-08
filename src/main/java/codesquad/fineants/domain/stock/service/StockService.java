package codesquad.fineants.domain.stock.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.dividend.service.StockDividendService;
import codesquad.fineants.domain.kis.repository.ClosingPriceRepository;
import codesquad.fineants.domain.kis.repository.CurrentPriceRepository;
import codesquad.fineants.domain.stock.domain.dto.request.StockSearchRequest;
import codesquad.fineants.domain.stock.domain.dto.response.StockReloadResponse;
import codesquad.fineants.domain.stock.domain.dto.response.StockResponse;
import codesquad.fineants.domain.stock.domain.dto.response.StockSearchItem;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockQueryRepository;
import codesquad.fineants.domain.stock.repository.StockRepository;
import codesquad.fineants.global.errors.errorcode.StockErrorCode;
import codesquad.fineants.global.errors.exception.NotFoundResourceException;
import codesquad.fineants.infra.s3.service.AmazonS3StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class StockService {
	private final StockRepository stockRepository;
	private final CurrentPriceRepository currentPriceRepository;
	private final ClosingPriceRepository closingPriceRepository;
	private final AmazonS3StockService amazonS3StockService;
	private final StockDividendService stockDividendService;
	private final StockQueryRepository stockQueryRepository;
	private final StockAndDividendManager stockAndDividendManager;

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
	public StockResponse getDetailedStock(String tickerSymbol) {
		Stock stock = stockRepository.findByTickerSymbol(tickerSymbol)
			.orElseThrow(() -> new NotFoundResourceException(StockErrorCode.NOT_FOUND_STOCK));
		return StockResponse.of(stock, currentPriceRepository, closingPriceRepository);
	}

	@Scheduled(cron = "0 0 8 * * ?") // 매일 오전 8시 (초, 분, 시간)
	@Transactional
	public void scheduledReloadStocks() {
		StockReloadResponse response = stockAndDividendManager.reloadStocks();
		log.info("refreshStocks response : {}", response);
		amazonS3StockService.writeStocks(stockRepository.findAll());
	}

	@Transactional
	public StockReloadResponse reloadStocks() {
		return stockAndDividendManager.reloadStocks();
	}
}
