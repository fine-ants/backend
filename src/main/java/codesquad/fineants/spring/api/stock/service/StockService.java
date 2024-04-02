package codesquad.fineants.spring.api.stock.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.portfolio_holding.PortfolioHoldingRepository;
import codesquad.fineants.domain.purchase_history.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.domain.stock_dividend.StockDividendRepository;
import codesquad.fineants.spring.api.common.errors.errorcode.StockErrorCode;
import codesquad.fineants.spring.api.common.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.krx.service.KRXService;
import codesquad.fineants.spring.api.stock.request.StockSearchRequest;
import codesquad.fineants.spring.api.stock.response.StockDataResponse;
import codesquad.fineants.spring.api.stock.response.StockRefreshResponse;
import codesquad.fineants.spring.api.stock.response.StockResponse;
import codesquad.fineants.spring.api.stock.response.StockSearchItem;
import codesquad.fineants.spring.api.stock.response.StockSectorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class StockService {
	private final StockRepository stockRepository;
	private final PortfolioHoldingRepository portfolioHoldingRepository;
	private final PurchaseHistoryRepository purchaseHistoryRepository;
	private final StockDividendRepository stockDividendRepository;
	private final CurrentPriceManager currentPriceManager;
	private final LastDayClosingPriceManager lastDayClosingPriceManager;
	private final KRXService krxService;

	public List<StockSearchItem> search(StockSearchRequest request) {
		return stockRepository.search(request.getSearchTerm())
			.stream()
			.map(StockSearchItem::from)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public StockResponse getStock(String tickerSymbol) {
		Stock stock = stockRepository.findByTickerSymbol(tickerSymbol)
			.orElseThrow(() -> new NotFoundResourceException(StockErrorCode.NOT_FOUND_STOCK));
		return StockResponse.of(stock, currentPriceManager, lastDayClosingPriceManager);
	}

	@Scheduled(cron = "0 0 8 * * ?") // 매일 오전 8시 (초, 분, 시간)
	@Transactional
	public void scheduledRefreshStocks() {
		StockRefreshResponse response = refreshStocks();
		log.info("refreshStocks response : {}", response);
	}

	// 최신 종목을 조회하고 데이터베이스의 종목 데이터들을 최신화한다
	@Transactional
	public StockRefreshResponse refreshStocks() {
		CompletableFuture<Set<StockDataResponse.StockIntegrationInfo>> future = CompletableFuture.supplyAsync(
			krxService::fetchStockInfo
		).thenCombine(CompletableFuture.supplyAsync(krxService::fetchSectorInfo),
			(latestStocks, sectorMap) ->
				latestStocks.parallelStream()
					.map(stock -> StockDataResponse.StockIntegrationInfo.from(stock,
						sectorMap.getOrDefault(stock.getTickerSymbol(), StockSectorResponse.SectorInfo.empty())
							.getSector()))
					.collect(Collectors.toSet())
		);

		// 기존 종목 정보 조회
		Set<StockDataResponse.StockIntegrationInfo> existStocks = stockRepository.findAll().parallelStream()
			.map(StockDataResponse.StockIntegrationInfo::from)
			.collect(Collectors.toSet());

		// 최신 종목 정보 조회
		Set<StockDataResponse.StockIntegrationInfo> latestStocks = future.join();

		// 종목 제거
		List<String> delTickerSymbols = removeDelistedStocksAndAssociatedData(existStocks, latestStocks);
		log.debug("delTickerSymbols count {}", delTickerSymbols.size());

		// 종목 저장
		List<String> newlyAddedTickerSymbols = saveNewlyAddedStocks(latestStocks, existStocks);
		log.debug("newlyAddedTickerSymbols count {}", newlyAddedTickerSymbols.size());

		return StockRefreshResponse.create(newlyAddedTickerSymbols, delTickerSymbols);
	}

	/**
	 * 상장 폐지된 종목들을 대상으로 db에 저장된 연관 데이터들을 삭제합니다.
	 * 연관 데이터들은 매입 이력, 포트폴리오에 등록된 종목, 종목 배당금, 종목이 해당됩니다.
	 * @param initialStockInfos 기존 종목 데이터
	 * @param latestStocks // 최신 종목 데이터
	 * @return 삭제된 종목의 티커 심볼 리스트
	 */
	private List<String> removeDelistedStocksAndAssociatedData(
		Set<StockDataResponse.StockIntegrationInfo> initialStockInfos,
		Set<StockDataResponse.StockIntegrationInfo> latestStocks) {
		Set<StockDataResponse.StockIntegrationInfo> delistedStocks = new HashSet<>(initialStockInfos);
		delistedStocks.removeAll(latestStocks);

		List<String> delTickerSymbols = delistedStocks.stream()
			.map(StockDataResponse.StockIntegrationInfo::getTickerSymbol)
			.collect(Collectors.toList());
		List<PortfolioHolding> holdings = portfolioHoldingRepository.findAllByTickerSymbolsWithStockAndPurchaseHistory(
			delTickerSymbols);
		List<Long> holdingIds = holdings.stream()
			.map(PortfolioHolding::getId)
			.collect(Collectors.toList());
		// 매입 이력 제거
		int delCount = purchaseHistoryRepository.deleteAllByHoldingIds(holdingIds);
		log.debug("delete purchaseHistory count {}", delCount);

		// 포트폴리오 종목 제거
		delCount = portfolioHoldingRepository.deleteAllByIdIn(holdingIds);
		log.debug("delete portfolioHolding count {}", delCount);

		// 종목 배당금 제거
		delCount = stockDividendRepository.deleteByTickerSymbols(delTickerSymbols);
		log.debug("delete stockDividend count {}", delCount);

		// 종목 제거
		delCount = stockRepository.deleteByTickerSymbols(delTickerSymbols);
		log.debug("delete stock count {}", delCount);
		return delTickerSymbols;
	}

	/**
	 * 새로 상장된 종목들을 대상으로 db에 저장합니다.
	 * @param latestStocks 최신 종목 데이터
	 * @param existStocks 기존 종목 데이터
	 * @return 상장된 종목의 티커 심볼 리스트
	 */
	private List<String> saveNewlyAddedStocks(Set<StockDataResponse.StockIntegrationInfo> latestStocks,
		Set<StockDataResponse.StockIntegrationInfo> existStocks) {
		// 새로 상장된 종목 파싱
		Set<StockDataResponse.StockIntegrationInfo> newlyAddedStocks = new HashSet<>(latestStocks);
		newlyAddedStocks.removeAll(existStocks);
		log.debug("newlyAddedStocks count {}", newlyAddedStocks.size());

		// 상장된 종목 추가
		List<Stock> stocks = newlyAddedStocks.parallelStream()
			.map(StockDataResponse.StockIntegrationInfo::toEntity)
			.collect(Collectors.toList());
		log.debug("stocks count {}", stocks.size());

		return stockRepository.saveAll(stocks).parallelStream()
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toList());
	}
}
