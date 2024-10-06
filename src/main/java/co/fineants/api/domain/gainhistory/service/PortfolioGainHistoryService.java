package co.fineants.api.domain.gainhistory.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.gainhistory.domain.dto.response.PortfolioGainHistoryCreateResponse;
import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.gainhistory.repository.PortfolioGainHistoryRepository;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class PortfolioGainHistoryService {
	private final PortfolioGainHistoryRepository repository;
	private final PortfolioRepository portfolioRepository;
	private final CurrentPriceRedisRepository currentPriceRedisRepository;

	@Transactional
	@CacheEvict(value = "lineChartCache", allEntries = true)
	public PortfolioGainHistoryCreateResponse addPortfolioGainHistory() {
		List<Portfolio> portfolios = portfolioRepository.findAll();
		List<PortfolioGainHistory> portfolioGainHistories = new ArrayList<>();
		PortfolioCalculator calculator = new PortfolioCalculator();
		for (Portfolio portfolio : portfolios) {
			portfolio.applyCurrentPriceAllHoldingsBy(currentPriceRedisRepository);
			PortfolioGainHistory latestHistory =
				repository.findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
						portfolio.getId(), LocalDateTime.now())
					.stream()
					.findFirst()
					.orElseGet(() -> PortfolioGainHistory.empty(portfolio));
			Expression totalGainExpr = calculator.calTotalGainBy(portfolio);
			Expression totalInvestment = calculator.calTotalInvestmentBy(portfolio);
			PortfolioGainHistory history = portfolio.createPortfolioGainHistory(latestHistory, totalGainExpr,
				totalInvestment);
			portfolioGainHistories.add(repository.save(history));
		}

		return PortfolioGainHistoryCreateResponse.from(portfolioGainHistories);
	}
}
