package co.fineants.api.domain.notification.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.common.notification.PortfolioTargetGainNotifiable;
import co.fineants.api.domain.common.notification.TargetPriceNotificationNotifiable;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;
import co.fineants.api.domain.stock_target_price.repository.StockTargetPriceRepository;
import co.fineants.api.global.errors.errorcode.PortfolioErrorCode;
import co.fineants.api.global.errors.exception.FineAntsException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotifiableFactory {

	private final PortfolioRepository portfolioRepository;
	private final StockTargetPriceRepository stockTargetPriceRepository;
	private final CurrentPriceRedisRepository currentPriceRedisRepository;

	public List<Notifiable> getAllPortfolios(Predicate<Portfolio> reachedPredicate) {
		return portfolioRepository.findAllWithAll().stream()
			.map(mapToNotifiable(reachedPredicate))
			.map(Notifiable.class::cast)
			.toList();
	}

	public Notifiable getPortfolio(Long portfolioId, Predicate<Portfolio> reachedPredicate) {
		return portfolioRepository.findByPortfolioIdWithAll(portfolioId)
			.map(mapToNotifiable(reachedPredicate))
			.map(Notifiable.class::cast)
			.orElseThrow(() -> new FineAntsException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
	}

	private Function<Portfolio, Notifiable> mapToNotifiable(Predicate<Portfolio> reachedPredicate) {
		return portfolio -> {
			boolean isReached = reachedPredicate.test(portfolio);
			return PortfolioTargetGainNotifiable.from(portfolio, isReached);
		};
	}

	public List<Notifiable> getAllTargetPriceNotificationsBy(Long memberId) {
		return stockTargetPriceRepository.findAllByMemberId(memberId)
			.stream()
			.map(StockTargetPrice::getTargetPriceNotifications)
			.flatMap(Collection::stream)
			.sorted(Comparator.comparingLong(TargetPriceNotification::getId))
			.map(targetPriceNotification -> {
				boolean isReached = targetPriceNotification.isSameTargetPrice(currentPriceRedisRepository);
				return TargetPriceNotificationNotifiable.from(targetPriceNotification, isReached);
			})
			.map(Notifiable.class::cast)
			.toList();
	}

	public List<Notifiable> getAllTargetPriceNotificationsBy(List<String> tickerSymbols) {
		return stockTargetPriceRepository.findAllByTickerSymbols(
				tickerSymbols)
			.stream()
			.map(StockTargetPrice::getTargetPriceNotifications)
			.flatMap(Collection::stream)
			.map(targetPriceNotification -> {
				boolean isReached = targetPriceNotification.isSameTargetPrice(currentPriceRedisRepository);
				return TargetPriceNotificationNotifiable.from(targetPriceNotification, isReached);
			})
			.map(Notifiable.class::cast)
			.toList();
	}
}
