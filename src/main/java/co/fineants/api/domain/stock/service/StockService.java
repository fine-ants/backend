package co.fineants.api.domain.stock.service;

import static co.fineants.api.infra.s3.service.AmazonS3StockService.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.dividend.repository.StockDividendRepository;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.stock.domain.dto.request.StockSearchRequest;
import co.fineants.api.domain.stock.domain.dto.response.StockReloadResponse;
import co.fineants.api.domain.stock.domain.dto.response.StockResponse;
import co.fineants.api.domain.stock.domain.dto.response.StockSearchItem;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.parser.StockParser;
import co.fineants.api.domain.stock.repository.StockQueryRepository;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.errors.errorcode.StockErrorCode;
import co.fineants.api.global.errors.exception.NotFoundResourceException;
import co.fineants.api.infra.s3.service.AmazonS3DividendService;
import co.fineants.api.infra.s3.service.AmazonS3StockService;
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
	private final StockParser stockParser;

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

	// csv 파일을 입력받아서 데이터베이스와 aws의 stocks.csv로 저장
	@Transactional
	public void initStocks(InputStream inputStream) {
		List<Stock> stocks;
		// Stock 리스트로 파싱
		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
			stocks = br.lines()
				.skip(1) // skip title
				.map(CSV_SEPARATOR_PATTERN::split)
				.map(stockParser::parse)
				.distinct()
				.toList();
		} catch (IOException e) {
			throw new IllegalArgumentException("fail parsing stock", e);
		}
		// 데이터베이스에 저장
		stockRepository.deleteAllInBatch();
		List<Stock> saveStocks = stockRepository.saveAll(stocks);

		// aws에 저장
		amazonS3StockService.writeStocks(saveStocks);
	}
}
