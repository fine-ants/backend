package co.fineants.api.domain.stock.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.dividend.repository.StockDividendRepository;
import co.fineants.api.domain.kis.domain.dto.response.KisSearchStockInfo;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.api.domain.stock.domain.dto.request.StockSearchRequest;
import co.fineants.api.domain.stock.domain.dto.response.StockReloadResponse;
import co.fineants.api.domain.stock.domain.dto.response.StockResponse;
import co.fineants.api.domain.stock.domain.dto.response.StockSearchItem;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockQueryRepository;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.common.delay.DelayManager;
import co.fineants.api.global.errors.errorcode.StockErrorCode;
import co.fineants.api.global.errors.exception.NotFoundResourceException;
import co.fineants.api.infra.s3.service.AmazonS3DividendService;
import co.fineants.api.infra.s3.service.AmazonS3StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class StockService {
	private final StockRepository stockRepository;
	private final CurrentPriceRedisRepository currentPriceRedisRepository;
	private final ClosingPriceRepository closingPriceRepository;
	private final AmazonS3StockService amazonS3StockService;
	private final AmazonS3DividendService amazonS3DividendService;
	private final StockQueryRepository stockQueryRepository;
	private final StockAndDividendManager stockAndDividendManager;
	private final StockDividendRepository stockDividendRepository;
	private final KisService kisService;
	private final DelayManager delayManager;

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
		return StockResponse.of(stock, currentPriceRedisRepository, closingPriceRepository);
	}

	@Transactional
	public StockReloadResponse reloadStocks() {
		StockReloadResponse response = stockAndDividendManager.reloadStocks();
		log.info("refreshStocks response : {}", response);
		amazonS3StockService.writeStocks(stockRepository.findAll());
		amazonS3DividendService.writeDividends(stockDividendRepository.findAll());
		return response;
	}

	/**
	 * 기존 모든 종목들을 최신 정보로 갱신한다
	 *
	 * @return 최신화된 종목 리스트
	 */
	@Transactional
	public List<Stock> syncAllStocksWithLatestData() {
		List<String> tickerSymbols = stockRepository.findAll().stream()
			.map(Stock::getTickerSymbol)
			.toList();
		List<Stock> result = new ArrayList<>();
		for (String ticker : tickerSymbols) {
			Mono<KisSearchStockInfo> mono = kisService.fetchSearchStockInfo(ticker);
			mono.map(KisSearchStockInfo::toEntity)
				.blockOptional(delayManager.timeout())
				.ifPresent(stock -> {
					Stock save = stockRepository.save(stock);
					result.add(save);
				});
		}
		amazonS3StockService.writeStocks(result);
		return result;
	}
}
