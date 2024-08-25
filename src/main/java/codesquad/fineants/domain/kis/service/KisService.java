package codesquad.fineants.domain.kis.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.holding.repository.PortfolioHoldingRepository;
import codesquad.fineants.domain.kis.aop.CheckedKisAccessToken;
import codesquad.fineants.domain.kis.client.KisAccessToken;
import codesquad.fineants.domain.kis.client.KisClient;
import codesquad.fineants.domain.kis.client.KisCurrentPrice;
import codesquad.fineants.domain.kis.domain.dto.response.KisClosingPrice;
import codesquad.fineants.domain.kis.domain.dto.response.KisDividend;
import codesquad.fineants.domain.kis.domain.dto.response.KisDividendWrapper;
import codesquad.fineants.domain.kis.domain.dto.response.KisIpo;
import codesquad.fineants.domain.kis.domain.dto.response.KisSearchStockInfo;
import codesquad.fineants.domain.kis.repository.ClosingPriceRepository;
import codesquad.fineants.domain.kis.repository.CurrentPriceRedisRepository;
import codesquad.fineants.domain.kis.repository.HolidayRepository;
import codesquad.fineants.domain.kis.repository.KisAccessTokenRepository;
import codesquad.fineants.domain.notification.event.publisher.PortfolioPublisher;
import codesquad.fineants.domain.stock.domain.dto.response.StockDataResponse;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockRepository;
import codesquad.fineants.domain.stock_target_price.domain.entity.StockTargetPrice;
import codesquad.fineants.domain.stock_target_price.event.publisher.StockTargetPricePublisher;
import codesquad.fineants.domain.stock_target_price.repository.StockTargetPriceRepository;
import codesquad.fineants.global.common.delay.DelayManager;
import codesquad.fineants.global.errors.exception.kis.CredentialsTypeKisException;
import codesquad.fineants.global.errors.exception.kis.ExpiredAccessTokenKisException;
import codesquad.fineants.global.errors.exception.kis.RequestLimitExceededKisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@RequiredArgsConstructor
@Service
public class KisService {
	public static final Duration DELAY = Duration.ofMillis(50L);
	public static final Duration TIMEOUT = Duration.ofMinutes(10L);
	private final KisClient kisClient;
	private final PortfolioHoldingRepository portFolioHoldingRepository;
	private final CurrentPriceRedisRepository currentPriceRedisRepository;
	private final ClosingPriceRepository closingPriceRepository;
	private final HolidayRepository holidayRepository;
	private final StockTargetPricePublisher stockTargetPricePublisher;
	private final PortfolioPublisher portfolioPublisher;
	private final StockTargetPriceRepository stockTargetPriceRepository;
	private final DelayManager delayManager;
	private final KisAccessTokenRepository kisAccessTokenRepository;
	private final KisAccessTokenRedisService kisAccessTokenRedisService;
	private final StockRepository stockRepository;

	// 회원이 가지고 있는 모든 종목에 대하여 현재가 갱신
	@Transactional
	@CheckedKisAccessToken
	public List<KisCurrentPrice> refreshAllStockCurrentPrice() {
		Set<String> totalTickerSymbol = new HashSet<>();
		totalTickerSymbol.addAll(portFolioHoldingRepository.findAllTickerSymbol());
		totalTickerSymbol.addAll(stockTargetPriceRepository.findAll().stream()
			.map(StockTargetPrice::getStock)
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toSet()));
		List<String> totalTickerSymbolList = totalTickerSymbol.stream().toList();
		List<KisCurrentPrice> prices = this.refreshStockCurrentPrice(totalTickerSymbolList);
		stockTargetPricePublisher.publishEvent(totalTickerSymbolList);
		portfolioPublisher.publishCurrentPriceEvent();
		return prices;
	}

	// 주식 현재가 갱신
	@Transactional(readOnly = true)
	@CheckedKisAccessToken
	public List<KisCurrentPrice> refreshStockCurrentPrice(List<String> tickerSymbols) {
		int concurrency = 20;
		List<KisCurrentPrice> prices = Flux.fromIterable(tickerSymbols)
			.flatMap(ticker -> this.fetchCurrentPrice(ticker)
				.doOnSuccess(kisCurrentPrice -> log.debug("reload stock current price {}", kisCurrentPrice))
				.onErrorResume(ExpiredAccessTokenKisException.class::isInstance, throwable -> Mono.empty())
				.onErrorResume(CredentialsTypeKisException.class::isInstance, throwable -> Mono.empty())
				.retryWhen(Retry.fixedDelay(5, delayManager.fixedDelay())
					.filter(RequestLimitExceededKisException.class::isInstance))
				.onErrorResume(Exceptions::isRetryExhausted, throwable -> Mono.empty()), concurrency)
			.delayElements(delayManager.delay())
			.collectList()
			.blockOptional(delayManager.timeout())
			.orElseGet(Collections::emptyList);
		currentPriceRedisRepository.savePrice(toArray(prices));
		log.info("The stock's current price has renewed {} out of {}", prices.size(), tickerSymbols.size());
		return prices;
	}

	private KisCurrentPrice[] toArray(List<KisCurrentPrice> prices) {
		return prices.toArray(KisCurrentPrice[]::new);
	}

	@CheckedKisAccessToken
	public Mono<KisCurrentPrice> fetchCurrentPrice(String tickerSymbol) {
		return Mono.defer(() -> kisClient.fetchCurrentPrice(tickerSymbol));
	}

	// start pm 15:30
	@Scheduled(cron = "* 30 15 * * *")
	@Transactional(readOnly = true)
	public void scheduledRefreshAllClosingPrice() {
		// 휴장일인 경우 실행하지 않음
		if (holidayRepository.isHoliday(LocalDate.now())) {
			return;
		}
		refreshAllClosingPrice();
	}

	@CheckedKisAccessToken
	public List<KisClosingPrice> refreshAllClosingPrice() {
		return refreshClosingPrice(stockRepository.findAll().stream()
			.map(Stock::getTickerSymbol)
			.toList());
	}

	@CheckedKisAccessToken
	public List<KisClosingPrice> refreshClosingPrice(List<String> tickerSymbols) {
		int concurrency = 20;
		List<KisClosingPrice> prices = Flux.fromIterable(tickerSymbols)
			.flatMap(ticker -> this.fetchClosingPrice(ticker)
				.doOnSuccess(price -> log.debug("reload stock closing price {}", price))
				.onErrorResume(ExpiredAccessTokenKisException.class::isInstance, throwable -> Mono.empty())
				.onErrorResume(CredentialsTypeKisException.class::isInstance, throwable -> Mono.empty())
				.retryWhen(Retry.fixedDelay(5, delayManager.fixedDelay())
					.filter(RequestLimitExceededKisException.class::isInstance))
				.onErrorResume(Exceptions::isRetryExhausted, throwable -> Mono.empty()), concurrency)
			.delayElements(delayManager.delay())
			.collectList()
			.blockOptional(delayManager.timeout())
			.orElseGet(Collections::emptyList);

		prices.forEach(closingPriceRepository::addPrice);
		log.info("종목 종가 {}개중 {}개 갱신", tickerSymbols.size(), prices.size());
		return prices;
	}

	@CheckedKisAccessToken
	public Mono<KisClosingPrice> fetchClosingPrice(String tickerSymbol) {
		return Mono.defer(() -> kisClient.fetchClosingPrice(tickerSymbol));
	}

	/**
	 * tickerSymbol에 해당하는 종목의 배당 일정을 조회합니다.
	 *
	 * @param tickerSymbol 종목 단축 코드
	 * @return 종목의 배당 일정 정보
	 */
	@CheckedKisAccessToken
	public List<KisDividend> fetchDividend(String tickerSymbol) {
		return kisClient.fetchDividendThisYear(tickerSymbol)
			.map(KisDividendWrapper::getKisDividends)
			.doOnSuccess(response -> log.debug("fetchDividend list is {}", response.size()))
			.onErrorResume(ExpiredAccessTokenKisException.class::isInstance, throwable -> Mono.empty())
			.onErrorResume(CredentialsTypeKisException.class::isInstance, throwable -> Mono.empty())
			.retryWhen(Retry.fixedDelay(5, delayManager.fixedDelay())
				.filter(RequestLimitExceededKisException.class::isInstance))
			.onErrorResume(Exceptions::isRetryExhausted, throwable -> Mono.empty())
			.blockOptional(delayManager.timeout())
			.orElseGet(Collections::emptyList);
	}

	@CheckedKisAccessToken
	public List<KisDividend> fetchDividendsBetween(LocalDate from, LocalDate to) {
		return kisClient.fetchDividendAll(from, to)
			.retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5)))
			.blockOptional(TIMEOUT)
			.orElseGet(Collections::emptyList).stream()
			.sorted()
			.toList();
	}

	/**
	 * 종목 기본 조회
	 *
	 * @param tickerSymbol 종목 티커 심볼
	 * @return 종목 정보
	 */
	@CheckedKisAccessToken
	public Mono<KisSearchStockInfo> fetchSearchStockInfo(String tickerSymbol) {
		return kisClient.fetchSearchStockInfo(tickerSymbol)
			.doOnSuccess(response -> log.debug("fetchSearchStockInfo ticker is {}", response.getTickerSymbol()))
			.retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5)))
			.onErrorResume(e -> Mono.empty());
	}

	/**
	 * 상장된 종목 조회
	 * 하루전부터 오늘까지의 상장된 종목들의 정보를 조회한다.
	 *
	 * @return 종목 정보 리스트
	 */
	@CheckedKisAccessToken
	public Set<StockDataResponse.StockIntegrationInfo> fetchStockInfoInRangedIpo() {
		LocalDate today = LocalDate.now();
		LocalDate yesterday = today.minusDays(1);
		Set<String> tickerSymbols = kisClient.fetchIpo(yesterday, today)
			.blockOptional(TIMEOUT)
			.orElseThrow()
			.getKisIpos().stream()
			.filter(kisIpo -> !kisIpo.isEmpty())
			.map(KisIpo::getShtCd)
			.collect(Collectors.toSet());

		int concurrency = 20;
		return Flux.fromIterable(tickerSymbols)
			.flatMap(this::fetchSearchStockInfo, concurrency)
			.delayElements(DELAY)
			.collectList()
			.blockOptional(TIMEOUT)
			.orElseGet(Collections::emptyList).stream()
			.map(KisSearchStockInfo::toEntity)
			.map(StockDataResponse.StockIntegrationInfo::from)
			.collect(Collectors.toUnmodifiableSet());
	}

	public KisAccessToken deleteAccessToken() {
		kisAccessTokenRepository.refreshAccessToken(null);
		KisAccessToken kisAccessToken = kisAccessTokenRedisService.getAccessTokenMap().orElse(null);
		kisAccessTokenRedisService.deleteAccessTokenMap();
		return kisAccessToken;
	}
}
