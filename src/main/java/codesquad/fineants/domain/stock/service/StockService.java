package codesquad.fineants.domain.stock.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.dividend.repository.StockDividendRepository;
import codesquad.fineants.domain.dividend.service.StockDividendService;
import codesquad.fineants.domain.kis.domain.dto.response.KisSearchStockInfo;
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
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class StockService {
	private final StockRepository stockRepository;
	private final StockDividendRepository dividendRepository;
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
		stockDividendService.writeDividendCsvToS3();
	}

	/**
	 * 종목 및 배당 일정을 최신화
	 * - 새로 상장된 종목 추가
	 * - 상장 폐지된 종목 삭제
	 * - 배당 일정 수정
	 * @return 상장 종목 및 폐지 종목 응답
	 */
	@Transactional
	public StockRefreshResponse reloadStocks() {
		// 상장된 종목 조회
		List<Stock> stocks = kisService.fetchStockInfoInRangedIpo().stream()
			.map(StockDataResponse.StockIntegrationInfo::toEntity)
			.toList();
		stocks.forEach(stock -> log.info("newlyAdded Stock : {}", stock));

		// 상장된 종목 저장
		Set<String> newlyAddedTickerSymbols = stockRepository.saveAll(stocks).stream()
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toUnmodifiableSet());
		newlyAddedTickerSymbols.forEach(ticker -> log.info("Save the item in the database and ticker : {}", ticker));

		// TODO: 전체 종목에 대해서 배당 일정을 조회하고 배당 일정을 저장한다
		// 1. 종목 전체 조회 및 티커 심볼 집합으로 변환
		Set<String> tickerSymbols = stockRepository.findAll()
			.stream()
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toUnmodifiableSet());

		// 2. 모든 종목의 현재가를 조회하여 폐지된 종목이 있는지 조회
		List<KisSearchStockInfo> currentStocks = new ArrayList<>(); // 상장 중인 종목
		List<KisSearchStockInfo> delistedStocks = new ArrayList<>();
		for (String tickerSymbol : tickerSymbols) {
			Mono<KisSearchStockInfo> mono = kisService.fetchSearchStockInfo(tickerSymbol);
			Optional<KisSearchStockInfo> stockInfo = mono.blockOptional(Duration.ofMinutes(1));
			// // 폐지된 종목이면 리스트에 추가
			stockInfo.filter(KisSearchStockInfo::isDelisted)
				.ifPresent(delistedStocks::add);
			// // 상장중인 종목이면 별도 리스트에 추가
			stockInfo.filter(KisSearchStockInfo::isListed)
				.ifPresent(currentStocks::add);
		}

		// 3. 상장 폐지된 종목의 배당금 제거
		Set<String> deletedStockCode = delistedStocks.stream()
			.map(KisSearchStockInfo::getStockCode)
			.collect(Collectors.toUnmodifiableSet());
		dividendRepository.deleteByStockCodes(deletedStockCode);
		log.info("delete dividends for StockCodes : {}", deletedStockCode);

		// 4. 상장 폐지된 종목 소프트 제거
		int deleted = stockRepository.deleteAllByStockCodes(deletedStockCode);
		log.info("delete stocks for TickerSymbols : {}, deleted={}", deletedStockCode, deleted);

		return StockRefreshResponse.create(newlyAddedTickerSymbols, deletedStockCode);
	}
}
