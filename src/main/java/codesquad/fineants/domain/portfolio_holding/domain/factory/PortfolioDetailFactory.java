package codesquad.fineants.domain.portfolio_holding.domain.factory;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import codesquad.fineants.domain.kis.repository.CurrentPriceRepository;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio_gain_history.domain.entity.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio_gain_history.repository.PortfolioGainHistoryRepository;
import codesquad.fineants.domain.portfolio_holding.domain.dto.response.PortfolioDetailRealTimeItem;
import codesquad.fineants.domain.portfolio_holding.domain.dto.response.PortfolioDetailResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortfolioDetailFactory {

	private final CurrentPriceRepository manager;
	private final PortfolioGainHistoryRepository portfolioGainHistoryRepository;

	public PortfolioDetailResponse createPortfolioDetailItem(Portfolio portfolio) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);
		PortfolioGainHistory history = portfolioGainHistoryRepository.findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
				portfolio.getId(), LocalDateTime.now())
			.stream()
			.findFirst()
			.orElseGet(PortfolioGainHistory::empty);
		return PortfolioDetailResponse.from(portfolio, history);
	}

	public PortfolioDetailRealTimeItem createPortfolioDetailRealTimeItem(Portfolio portfolio) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);
		PortfolioGainHistory history = portfolioGainHistoryRepository.findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
				portfolio.getId(), LocalDateTime.now())
			.stream()
			.findFirst()
			.orElseGet(PortfolioGainHistory::empty);
		return PortfolioDetailRealTimeItem.of(portfolio, history);
	}
}
