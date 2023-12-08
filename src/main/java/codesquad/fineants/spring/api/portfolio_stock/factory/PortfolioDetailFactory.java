package codesquad.fineants.spring.api.portfolio_stock.factory;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistoryRepository;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioDetailRealTimeItem;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioDetailResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortfolioDetailFactory {

	private final CurrentPriceManager manager;
	private final PortfolioGainHistoryRepository portfolioGainHistoryRepository;

	public PortfolioDetailResponse createPortfolioDetailItem(Portfolio portfolio) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);
		PortfolioGainHistory history = portfolioGainHistoryRepository.findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
			portfolio.getId(), LocalDateTime.now()).orElseGet(PortfolioGainHistory::empty);
		return PortfolioDetailResponse.from(portfolio, history);
	}

	public PortfolioDetailRealTimeItem createPortfolioDetailRealTimeItem(Portfolio portfolio) {
		portfolio.applyCurrentPriceAllHoldingsBy(manager);
		PortfolioGainHistory history = portfolioGainHistoryRepository.findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
			portfolio.getId(), LocalDateTime.now()).orElseGet(PortfolioGainHistory::empty);
		return PortfolioDetailRealTimeItem.of(portfolio, history);
	}
}
