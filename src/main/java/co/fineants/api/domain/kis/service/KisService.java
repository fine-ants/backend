package co.fineants.api.domain.kis.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.kis.client.KisAccessToken;
import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.client.KisWebSocketApprovalKey;
import co.fineants.api.domain.kis.domain.dto.response.KisClosingPrice;
import co.fineants.api.domain.kis.domain.dto.response.KisDividend;
import co.fineants.api.domain.kis.domain.dto.response.KisDividendWrapper;
import co.fineants.api.domain.kis.domain.dto.response.KisIpo;
import co.fineants.api.domain.kis.domain.dto.response.KisIpoResponse;
import co.fineants.api.domain.kis.domain.dto.response.KisSearchStockInfo;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.kis.repository.KisAccessTokenRepository;
import co.fineants.api.domain.notification.event.publisher.PortfolioPublisher;
import co.fineants.api.domain.stock.domain.dto.response.StockDataResponse;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import co.fineants.api.domain.stock_target_price.event.publisher.StockTargetPricePublisher;
import co.fineants.api.domain.stock_target_price.repository.StockTargetPriceRepository;
import co.fineants.api.global.common.delay.DelayManager;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.global.errors.exception.kis.CredentialsTypeKisException;
import co.fineants.api.global.errors.exception.kis.ExpiredAccessTokenKisException;
import co.fineants.api.global.errors.exception.kis.RequestLimitExceededKisException;
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
	private final KisClient kisClient;
	private final PortfolioHoldingRepository portFolioHoldingRepository;
	private final CurrentPriceRedisRepository currentPriceRedisRepository;
	private final ClosingPriceRepository closingPriceRepository;
	private final StockTargetPricePublisher stockTargetPricePublisher;
	private final PortfolioPublisher portfolioPublisher;
	private final StockTargetPriceRepository stockTargetPriceRepository;
	private final DelayManager delayManager;
	private final KisAccessTokenRepository kisAccessTokenRepository;
	private final KisAccessTokenRedisService kisAccessTokenRedisService;
	private final StockRepository stockRepository;
	private final LocalDateTimeService localDateTimeService;

	// 회원이 가지고 있는 모든 종목에 대하여 현재가 갱신
	@Transactional
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

	public Mono<KisCurrentPrice> fetchCurrentPrice(String tickerSymbol) {
		return Mono.defer(() -> kisClient.fetchCurrentPrice(tickerSymbol));
	}

	public List<KisClosingPrice> refreshAllClosingPrice() {
		return refreshClosingPrice(stockRepository.findAll().stream()
			.map(Stock::getTickerSymbol)
			.toList());
	}

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

	public Mono<KisClosingPrice> fetchClosingPrice(String tickerSymbol) {
		return Mono.defer(() -> kisClient.fetchClosingPrice(tickerSymbol));
	}

	/**
	 * tickerSymbol에 해당하는 종목의 배당 일정을 조회합니다.
	 *
	 * @param tickerSymbol 종목 단축 코드
	 * @return 종목의 배당 일정 정보
	 */
	public Flux<KisDividend> fetchDividend(String tickerSymbol) {
		return kisClient.fetchDividendThisYear(tickerSymbol)
			.map(KisDividendWrapper::getKisDividends)
			.doOnSuccess(response -> log.debug("fetchDividend list is {}", response.size()))
			.onErrorResume(ExpiredAccessTokenKisException.class::isInstance, throwable -> Mono.empty())
			.onErrorResume(CredentialsTypeKisException.class::isInstance, throwable -> Mono.empty())
			.retryWhen(Retry.fixedDelay(5, delayManager.fixedDelay())
				.filter(RequestLimitExceededKisException.class::isInstance))
			.onErrorResume(Exceptions::isRetryExhausted, throwable -> Mono.empty())
			.onErrorResume(throwable -> Mono.empty())
			.flatMapMany(Flux::fromIterable);
	}

	public List<KisDividend> fetchDividendsBetween(LocalDate from, LocalDate to) {
		return kisClient.fetchDividendsBetween(from, to)
			.doOnSuccess(dividends -> log.debug("dividends is {}", dividends))
			.onErrorResume(ExpiredAccessTokenKisException.class::isInstance, throwable -> Mono.empty())
			.onErrorResume(CredentialsTypeKisException.class::isInstance, throwable -> Mono.empty())
			.retryWhen(Retry.fixedDelay(5, delayManager.fixedDelay())
				.filter(RequestLimitExceededKisException.class::isInstance))
			.onErrorResume(Exceptions::isRetryExhausted, throwable -> Mono.empty())
			.blockOptional(delayManager.timeout())
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
	public Mono<KisSearchStockInfo> fetchSearchStockInfo(String tickerSymbol) {
		return kisClient.fetchSearchStockInfo(tickerSymbol)
			.doOnSuccess(response -> log.debug("fetchSearchStockInfo ticker is {}", response.getTickerSymbol()))
			.onErrorResume(ExpiredAccessTokenKisException.class::isInstance, throwable -> Mono.empty())
			.onErrorResume(CredentialsTypeKisException.class::isInstance, throwable -> Mono.empty())
			.retryWhen(Retry.fixedDelay(5, delayManager.fixedDelay())
				.filter(RequestLimitExceededKisException.class::isInstance))
			.onErrorResume(Exceptions::isRetryExhausted, throwable -> Mono.empty())
			.onErrorResume(throwable -> {
				log.error("fetchSearchStockInfo error message is {}", throwable.getMessage());
				return Mono.empty();
			});
	}

	/**
	 * 상장된 종목 조회
	 * 하루전부터 오늘까지의 상장된 종목들의 정보를 조회한다.
	 *
	 * @return 종목 정보 리스트
	 */
	public Flux<StockDataResponse.StockIntegrationInfo> fetchStockInfoInRangedIpo() {
		LocalDate today = localDateTimeService.getLocalDateWithNow();
		LocalDate yesterday = today.minusDays(1);
		Flux<String> tickerSymbols = kisClient.fetchIpo(yesterday, today)
			.onErrorResume(throwable -> {
				log.error("fetchIpo error message is {}", throwable.getMessage());
				return Mono.empty();
			})
			.map(KisIpoResponse::getKisIpos)
			.defaultIfEmpty(Collections.emptyList())
			.flatMapMany(Flux::fromIterable)
			.filter(kisIpo -> !kisIpo.isEmpty())
			.map(KisIpo::getShtCd);

		int concurrency = 20;
		return tickerSymbols
			.flatMap(this::fetchSearchStockInfo, concurrency)
			.delayElements(delayManager.delay())
			.onErrorResume(throwable -> {
				log.error("fetchSearchStockInfo error message is {}", throwable.getMessage());
				return Mono.empty();
			})
			.map(KisSearchStockInfo::toEntity)
			.map(StockDataResponse.StockIntegrationInfo::from);
	}

	public KisAccessToken deleteAccessToken() {
		kisAccessTokenRepository.refreshAccessToken(null);
		KisAccessToken kisAccessToken = kisAccessTokenRedisService.getAccessTokenMap().orElse(null);
		kisAccessTokenRedisService.deleteAccessTokenMap();
		return kisAccessToken;
	}

	public Optional<String> fetchApprovalKey() {
		return kisClient.fetchWebSocketApprovalKey()
			.retryWhen(Retry.fixedDelay(5, delayManager.fixedAccessTokenDelay()))
			.onErrorResume(throwable -> {
				log.error(throwable.getMessage());
				return Mono.empty();
			})
			.log()
			.blockOptional(delayManager.timeout())
			.map(KisWebSocketApprovalKey::getApprovalKey);
	}
}
