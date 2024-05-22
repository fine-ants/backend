package codesquad.fineants.domain.portfolio.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.common.money.Bank;
import codesquad.fineants.domain.common.money.Currency;
import codesquad.fineants.domain.common.money.Expression;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.RateDivision;
import codesquad.fineants.domain.kis.repository.CurrentPriceRepository;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.portfolio.domain.dto.response.DashboardLineChartResponse;
import codesquad.fineants.domain.portfolio.domain.dto.response.DashboardPieChartResponse;
import codesquad.fineants.domain.portfolio.domain.dto.response.OverviewResponse;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.repository.PortfolioRepository;
import codesquad.fineants.domain.portfolio_gain_history.domain.entity.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio_gain_history.repository.PortfolioGainHistoryRepository;
import codesquad.fineants.global.errors.errorcode.MemberErrorCode;
import codesquad.fineants.global.errors.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {
	private final PortfolioRepository portfolioRepository;
	private final MemberRepository memberRepository;
	private final CurrentPriceRepository currentPriceRepository;
	private final PortfolioGainHistoryRepository portfolioGainHistoryRepository;

	@Transactional(readOnly = true)
	public OverviewResponse getOverview(Long memberId) {
		List<Portfolio> portfolios = portfolioRepository.findAllByMemberId(memberId);
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new BadRequestException(MemberErrorCode.NOT_FOUND_MEMBER));
		Expression totalValuation = Money.wonZero();
		Expression totalCurrentValuation = Money.wonZero();
		Expression totalInvestment = Money.wonZero();
		Expression totalGain = Money.wonZero();
		Expression totalAnnualDividend = Money.wonZero();
		if (portfolios.isEmpty()) {
			return OverviewResponse.empty(member.getNickname());
		}
		for (Portfolio portfolio : portfolios) {
			portfolio.applyCurrentPriceAllHoldingsBy(currentPriceRepository);
			totalValuation = totalValuation.plus(portfolio.calculateTotalAsset());
			totalCurrentValuation = totalCurrentValuation.plus(portfolio.calculateTotalCurrentValuation());
			totalInvestment = totalInvestment.plus(portfolio.calculateTotalInvestmentAmount());
			totalGain = totalGain.plus(portfolio.calculateTotalGain());
			totalAnnualDividend = totalAnnualDividend.plus(portfolio.calculateAnnualDividend());
		}
		RateDivision totalAnnualDividendYield = totalAnnualDividend.divide(totalCurrentValuation);
		RateDivision totalGainRate = totalGain.divide(totalInvestment);

		return OverviewResponse.of(
			member.getNickname(),
			totalValuation,
			totalInvestment,
			totalGain,
			totalGainRate.toPercentage(Bank.getInstance(), Currency.KRW),
			totalAnnualDividend,
			totalAnnualDividendYield.toPercentage(Bank.getInstance(), Currency.KRW)
		);
	}

	@Transactional(readOnly = true)
	public List<DashboardPieChartResponse> getPieChart(Long memberId) {
		List<Portfolio> portfolios = portfolioRepository.findAllByMemberId(memberId);
		if (portfolios.isEmpty()) {
			return new ArrayList<>();
		}
		Expression totalValuation = Money.wonZero();// 평가 금액 + 현금
		for (Portfolio portfolio : portfolios) {
			portfolio.applyCurrentPriceAllHoldingsBy(currentPriceRepository);
			totalValuation = totalValuation.plus(portfolio.calculateTotalAsset());
		}
		List<DashboardPieChartResponse> pieChartResponses = new ArrayList<>();
		for (Portfolio portfolio : portfolios) {
			pieChartResponses.add(DashboardPieChartResponse.of(portfolio, totalValuation));
		}
		// 정렬
		// 1. 가치(평가금액+현금) 기준 내림차순
		// 2. 총손익 기준 내림차순
		Bank bank = Bank.getInstance();
		pieChartResponses.sort(
			((Comparator<DashboardPieChartResponse>)(o1, o2) -> {
				Money m1 = bank.toWon(o1.getValuation());
				Money m2 = bank.toWon(o2.getValuation());
				return m2.compareTo(m1);
			})
				.thenComparing((o1, o2) -> {
					Money m1 = bank.toWon(o1.getTotalGain());
					Money m2 = bank.toWon(o2.getTotalGain());
					return m2.compareTo(m1);
				}));
		return pieChartResponses;
	}

	@Transactional(readOnly = true)
	public List<DashboardLineChartResponse> getLineChart(Long memberId) {
		List<Portfolio> portfolios = portfolioRepository.findAllByMemberId(memberId);
		if (portfolios.isEmpty()) {
			return new ArrayList<>();
		}
		List<PortfolioGainHistory> portfolioGainHistories = new ArrayList<>();
		for (Portfolio portfolio : portfolios) {
			portfolioGainHistories.addAll(portfolioGainHistoryRepository.findAllByPortfolioId(portfolio.getId()));
		}
		Map<String, Expression> timeValueMap = new HashMap<>();
		for (PortfolioGainHistory portfolioGainHistory : portfolioGainHistories) {
			String time = portfolioGainHistory.getCreateAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			timeValueMap.put(time, timeValueMap.getOrDefault(time, Money.zero())
				.plus(portfolioGainHistory.getCash())
				.plus(portfolioGainHistory.getCurrentValuation()));
		}
		return timeValueMap.keySet()
			.stream()
			.sorted()
			.map(key -> DashboardLineChartResponse.of(key, timeValueMap.get(key)))
			.collect(
				Collectors.toList());
	}
}
