package co.fineants.api.domain.holding.domain.factory;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.gainhistory.repository.PortfolioGainHistoryRepository;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioDetailRealTimeItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioDetailResponse;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.global.common.time.LocalDateTimeService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortfolioDetailFactory {

	private final CurrentPriceRedisRepository manager;
	private final PortfolioGainHistoryRepository portfolioGainHistoryRepository;
	private final LocalDateTimeService localDateTimeService;

	public PortfolioDetailResponse createPortfolioDetailItem(Portfolio portfolio) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);
		PortfolioGainHistory history =
			portfolioGainHistoryRepository.findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
					portfolio.getId(), LocalDateTime.now())
				.stream()
				.findFirst()
				.orElseGet(() -> PortfolioGainHistory.empty(portfolio));
		return PortfolioDetailResponse.from(portfolio, history, localDateTimeService);
	}

	public PortfolioDetailRealTimeItem createPortfolioDetailRealTimeItem(Portfolio portfolio) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);
		PortfolioGainHistory history =
			portfolioGainHistoryRepository.findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
					portfolio.getId(), LocalDateTime.now())
				.stream()
				.findFirst()
				.orElseGet(() -> PortfolioGainHistory.empty(portfolio));
		return PortfolioDetailRealTimeItem.of(portfolio, history);
	}
}
