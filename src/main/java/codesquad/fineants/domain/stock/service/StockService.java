package codesquad.fineants.domain.stock.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.dividend.repository.StockDividendRepository;
import codesquad.fineants.domain.kis.repository.ClosingPriceRepository;
import codesquad.fineants.domain.kis.repository.CurrentPriceRedisRepository;
import codesquad.fineants.domain.stock.domain.dto.request.StockSearchRequest;
import codesquad.fineants.domain.stock.domain.dto.response.StockReloadResponse;
import codesquad.fineants.domain.stock.domain.dto.response.StockResponse;
import codesquad.fineants.domain.stock.domain.dto.response.StockSearchItem;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockQueryRepository;
import codesquad.fineants.domain.stock.repository.StockRepository;
import codesquad.fineants.global.errors.errorcode.StockErrorCode;
import codesquad.fineants.global.errors.exception.NotFoundResourceException;
import codesquad.fineants.infra.s3.service.AmazonS3DividendService;
import codesquad.fineants.infra.s3.service.AmazonS3StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
}
