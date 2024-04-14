package codesquad.fineants.spring.api.kis.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.portfolio_holding.PortfolioHoldingRepository;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.domain.stock_dividend.StockDividend;
import codesquad.fineants.domain.stock_dividend.StockDividendRepository;
import codesquad.fineants.spring.api.common.errors.exception.KisException;
import codesquad.fineants.spring.api.kis.client.KisClient;
import codesquad.fineants.spring.api.kis.client.KisCurrentPrice;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.HolidayManager;
import codesquad.fineants.spring.api.kis.manager.KisAccessTokenManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.kis.response.KisClosingPrice;
import codesquad.fineants.spring.api.kis.response.KisDividend;
import codesquad.fineants.spring.api.notification.event.PortfolioPublisher;
import codesquad.fineants.spring.api.stock_target_price.event.StockTargetPricePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class KisService {
	private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	public static final Duration TIMEOUT = Duration.ofMinutes(1L);

	private final KisClient kisClient;
	private final PortfolioHoldingRepository portFolioHoldingRepository;
	private final KisAccessTokenManager manager;
	private final CurrentPriceManager currentPriceManager;
	private final LastDayClosingPriceManager lastDayClosingPriceManager;
	private final HolidayManager holidayManager;
	private final StockTargetPricePublisher stockTargetPricePublisher;
	private final PortfolioPublisher portfolioPublisher;
	private final StockRepository stockRepository;
	private final StockDividendRepository stockDividendRepository;

	// 평일 9am ~ 15:59pm 5초마다 현재가 갱신 수행
	@Scheduled(cron = "0/5 * 9-15 ? * MON,TUE,WED,THU,FRI")
	public void scheduleRefreshingAllStockCurrentPrice() {
		// 휴장일인 경우 실행하지 않음
		if (holidayManager.isHoliday(LocalDate.now())) {
			return;
		}
		refreshAllStockCurrentPrice();
	}

	// 회원이 가지고 있는 모든 종목에 대하여 현재가 갱신
	public List<KisCurrentPrice> refreshAllStockCurrentPrice() {
		List<String> tickerSymbols = portFolioHoldingRepository.findAllTickerSymbol();
		List<KisCurrentPrice> prices = refreshStockCurrentPrice(tickerSymbols);
		stockTargetPricePublisher.publishEvent(tickerSymbols);
		portfolioPublisher.publishCurrentPriceEvent();
		return prices;
	}

	// 주식 현재가 갱신
	public List<KisCurrentPrice> refreshStockCurrentPrice(List<String> tickerSymbols) {
		List<CompletableFuture<KisCurrentPrice>> futures = tickerSymbols.stream()
			.map(this::submitCurrentPriceFuture)
			.collect(Collectors.toList());

		List<KisCurrentPrice> prices = futures.stream()
			.map(future -> {
				try {
					return future.get(1L, TimeUnit.MINUTES);
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					return null;
				}
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		prices.forEach(currentPriceManager::addCurrentPrice);

		log.info("종목 현재가 {}개중 {}개 갱신", tickerSymbols.size(), prices.size());
		return prices;
	}

	private CompletableFuture<KisCurrentPrice> submitCurrentPriceFuture(String tickerSymbol) {
		CompletableFuture<KisCurrentPrice> future = createCompletableFuture();
		executorService.schedule(createCurrentPriceRunnable(tickerSymbol, future), 1L, TimeUnit.SECONDS);
		return future;
	}

	private <T> CompletableFuture<T> createCompletableFuture() {
		CompletableFuture<T> future = new CompletableFuture<>();
		future.orTimeout(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
		future.exceptionally(e -> {
			log.error(e.getMessage());
			return null;
		});
		return future;
	}

	private Runnable createCurrentPriceRunnable(String tickerSymbol, CompletableFuture<KisCurrentPrice> future) {
		return () -> {
			try {
				future.complete(fetchCurrentPrice(tickerSymbol)
					.blockOptional(Duration.ofMinutes(1L))
					.orElseGet(() -> KisCurrentPrice.empty(tickerSymbol))
				);
			} catch (KisException e) {
				future.completeExceptionally(e);
			}
		};
	}

	public Mono<KisCurrentPrice> fetchCurrentPrice(String tickerSymbol) {
		return kisClient.fetchCurrentPrice(tickerSymbol, manager.createAuthorization());
	}

	// 15시 30분에 종가 갱신 수행
	@Scheduled(cron = "* 30 15 * * *")
	public void scheduleRefreshingAllLastDayClosingPrice() {
		// 휴장일인 경우 실행하지 않음
		if (holidayManager.isHoliday(LocalDate.now())) {
			return;
		}
		refreshAllLastDayClosingPrice();
	}

	// 종목 종가 모두 갱신
	public List<KisClosingPrice> refreshAllLastDayClosingPrice() {
		List<String> tickerSymbols = portFolioHoldingRepository.findAllTickerSymbol();
		return refreshLastDayClosingPrice(tickerSymbols);
	}

	// 종목 종가 일부 갱신
	public List<KisClosingPrice> refreshLastDayClosingPrice(List<String> tickerSymbols) {
		List<KisClosingPrice> lastDayClosingPrices = readLastDayClosingPriceResponses(tickerSymbols);
		lastDayClosingPrices.forEach(lastDayClosingPriceManager::addPrice);
		log.info("종목 종가 {}개중 {}개 갱신", tickerSymbols.size(), lastDayClosingPrices.size());
		return lastDayClosingPrices;
	}

	private List<KisClosingPrice> readLastDayClosingPriceResponses(List<String> unknownTickerSymbols) {
		List<CompletableFuture<KisClosingPrice>> futures = unknownTickerSymbols.stream()
			.map(this::createClosingPriceFuture)
			.collect(Collectors.toList());

		return futures.stream()
			.map(future -> {
				try {
					return future.get(1L, TimeUnit.MINUTES);
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					return null;
				}
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	private CompletableFuture<KisClosingPrice> createClosingPriceFuture(
		String tickerSymbol) {
		CompletableFuture<KisClosingPrice> future = createCompletableFuture();
		executorService.schedule(() -> {
			try {
				future.complete(fetchClosingPrice(tickerSymbol)
					.blockOptional(TIMEOUT)
					.orElseGet(() -> KisClosingPrice.empty(tickerSymbol)));
			} catch (KisException e) {
				future.completeExceptionally(e);
			}
		}, 1L, TimeUnit.SECONDS);
		return future;
	}

	public Mono<KisClosingPrice> fetchClosingPrice(String tickerSymbol) {
		return kisClient.fetchClosingPrice(tickerSymbol, manager.createAuthorization());
	}

	public String fetchDividend(String tickerSymbol) {
		return kisClient.fetchDividend(tickerSymbol, manager.createAuthorization());
	}

	@Transactional
	public void refreshDividendSchedule(LocalDate now) {
		// 1. 한국투자증권 서버로부터 배당 일정을 조회
		LocalDate from = now.minusYears(1L).with(TemporalAdjusters.firstDayOfYear());
		LocalDate to = now.with(TemporalAdjusters.lastDayOfYear());
		Map<String, List<KisDividend>> kisDividendMap = kisClient.fetchDividend(from, to, manager.createAuthorization())
			.stream()
			.collect(Collectors.groupingBy(KisDividend::getTickerSymbol));
		log.debug("kisDividendMap : {}", kisDividendMap);

		// 2. 기존 DB에서 모든 배당 일정 로드
		Map<String, List<StockDividend>> stockDividendMap = stockRepository.findAllWithDividends().stream()
			.collect(Collectors.toMap(Stock::getTickerSymbol, Stock::getStockDividends));
		log.debug("stockDividendMap : {}", stockDividendMap);

		// 3. 최신화 작업 수행
		List<StockDividend> updateStockDividend = new ArrayList<>();
		List<StockDividend> deleteStockDividend = new ArrayList<>();
		for (Map.Entry<String, List<KisDividend>> entry : kisDividendMap.entrySet()) {
			String tickerSymbol = entry.getKey();
			List<KisDividend> newDividends = entry.getValue();

			List<StockDividend> existingDividends = stockDividendMap.getOrDefault(tickerSymbol,
				new ArrayList<>());

			for (KisDividend newDividend : newDividends) {
				// 4. 새로운 일정이 기존에 없는 경우 추가
				Optional<StockDividend> optionalStockDividend = findDividend(existingDividends, newDividend);

				Optional<Stock> optionalStock = stockRepository.findByTickerSymbol(newDividend.getTickerSymbol());
				if (optionalStock.isEmpty()) {
					break;
				}
				Stock stock = optionalStock.get();
				StockDividend newStockDividend = newDividend.toEntity(stock);
				if (optionalStockDividend.isEmpty()) {
					existingDividends.add(newStockDividend);
				} else {
					// 새로운 일정이 기존에 있는 경우 업데이트 수행 (현금 배당 지급일 등)
					updateStockDividendFrom(optionalStockDividend.get(), newStockDividend);
				}
			}

			// 5. 업데이트하거나 새로 추가된 배당 일정들 필터링
			updateStockDividend.addAll(existingDividends.stream()
				.filter(stockDividend -> containsDividend(newDividends, stockDividend))
				.collect(Collectors.toList())
			);

			// 7. 기존의 일정이 새로운 일정에 없는 배당금들 필터링
			deleteStockDividend.addAll(existingDividends.stream()
				.filter(stockDividend -> !containsDividend(newDividends, stockDividend))
				.collect(Collectors.toList())
			);
		}

		// 6. 업데이트된 최신화 배당 일정을 데이터베이스에 반영
		log.debug("updateStockDividend : {}", updateStockDividend);
		stockDividendRepository.saveAll(updateStockDividend);

		// 8. 최신 배당 일정에 없는 배당일정들 제거
		log.debug("deleteStockDividend : {}", deleteStockDividend);
		stockDividendRepository.deleteAll(deleteStockDividend);
	}

	// stockDividends 리스트에서 KisDividend 객체가 가진 동일한 티커심볼과 배당기준일이 동일한 StockDividend를 탐색
	private Optional<StockDividend> findDividend(List<StockDividend> stockDividends, KisDividend kisDividend) {
		return stockDividends.stream()
			.filter(stockDividend -> stockDividend.equalTickerSymbolAndRecordDate(kisDividend))
			.findAny();
	}

	// from 정보를 to 정보로 업데이트
	private void updateStockDividendFrom(StockDividend from, StockDividend to) {
		from.change(to);
	}

	// KisDividend 리스트에 stockDividend 정보가 포함되어 있는지 검사
	private boolean containsDividend(List<KisDividend> kisDividends, StockDividend stockDividend) {
		for (KisDividend kisDividend : kisDividends) {
			if (stockDividend.equalTickerSymbolAndRecordDate(kisDividend)) {
				return true;
			}
		}
		return false;
	}
}
