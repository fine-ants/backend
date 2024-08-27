package codesquad.fineants.domain.gainhistory.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.gainhistory.domain.dto.response.PortfolioGainHistoryCreateResponse;
import codesquad.fineants.domain.gainhistory.domain.entity.PortfolioGainHistory;
import codesquad.fineants.domain.gainhistory.repository.PortfolioGainHistoryRepository;
import codesquad.fineants.domain.kis.repository.CurrentPriceRedisRepository;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.repository.PortfolioRepository;
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
	public PortfolioGainHistoryCreateResponse addPortfolioGainHistory() {
		List<Portfolio> portfolios = portfolioRepository.findAll();
		List<PortfolioGainHistory> portfolioGainHistories = new ArrayList<>();

		for (Portfolio portfolio : portfolios) {
			portfolio.applyCurrentPriceAllHoldingsBy(currentPriceRedisRepository);
			PortfolioGainHistory latestHistory =
				repository.findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
						portfolio.getId(), LocalDateTime.now())
					.stream()
					.findFirst()
					.orElseGet(() -> PortfolioGainHistory.empty(portfolio));
			PortfolioGainHistory history = portfolio.createPortfolioGainHistory(latestHistory);
			portfolioGainHistories.add(repository.save(history));
		}

		return PortfolioGainHistoryCreateResponse.from(portfolioGainHistories);
	}
}
