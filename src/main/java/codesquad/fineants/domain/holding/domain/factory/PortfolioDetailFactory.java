package codesquad.fineants.domain.holding.domain.factory;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import codesquad.fineants.domain.gainhistory.domain.entity.PortfolioGainHistory;
import codesquad.fineants.domain.gainhistory.repository.PortfolioGainHistoryRepository;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioDetailRealTimeItem;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioDetailResponse;
import codesquad.fineants.domain.kis.repository.CurrentPriceRedisRepository;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortfolioDetailFactory {

	private final CurrentPriceRedisRepository manager;
	private final PortfolioGainHistoryRepository portfolioGainHistoryRepository;

	public PortfolioDetailResponse createPortfolioDetailItem(Portfolio portfolio) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);
		PortfolioGainHistory history =
			portfolioGainHistoryRepository.findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
					portfolio.getId(), LocalDateTime.now())
				.stream()
				.findFirst()
				.orElseGet(() -> PortfolioGainHistory.empty(portfolio));
		return PortfolioDetailResponse.from(portfolio, history);
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
