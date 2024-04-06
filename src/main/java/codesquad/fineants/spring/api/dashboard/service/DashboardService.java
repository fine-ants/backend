package codesquad.fineants.spring.api.dashboard.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistoryRepository;
import codesquad.fineants.spring.api.common.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.common.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.dashboard.response.DashboardLineChartResponse;
import codesquad.fineants.spring.api.dashboard.response.DashboardPieChartResponse;
import codesquad.fineants.spring.api.dashboard.response.OverviewResponse;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {
	private final PortfolioRepository portfolioRepository;
	private final MemberRepository memberRepository;
	private final CurrentPriceManager currentPriceManager;
	private final PortfolioGainHistoryRepository portfolioGainHistoryRepository;

	@Transactional(readOnly = true)
	public OverviewResponse getOverview(AuthMember authMember) {
		List<Portfolio> portfolios = portfolioRepository.findAllByMemberId(authMember.getMemberId());
		Member member = memberRepository.findById(authMember.getMemberId())
			.orElseThrow(() -> new BadRequestException(MemberErrorCode.NOT_FOUND_MEMBER));
		Money totalValuation = Money.zero();
		Money totalCurrentValuation = Money.zero();
		Money totalInvestment = Money.zero();
		Money totalGain = Money.zero();
		Money totalAnnualDividend = Money.zero();
		if (portfolios.isEmpty()) {
			return OverviewResponse.empty(member.getNickname());
		}
		for (Portfolio portfolio : portfolios) {
			portfolio.applyCurrentPriceAllHoldingsBy(currentPriceManager);
			totalValuation = totalValuation.add(portfolio.calculateTotalAsset());
			totalCurrentValuation = totalCurrentValuation.add(portfolio.calculateTotalCurrentValuation());
			totalInvestment = totalInvestment.add(portfolio.calculateTotalInvestmentAmount());
			totalGain = totalGain.add(portfolio.calculateTotalGain());
			totalAnnualDividend = totalAnnualDividend.add(portfolio.calculateAnnualDividend());
		}
		double totalAnnualDividendYield = totalAnnualDividend.divide(totalCurrentValuation).toPercentage();
		double totalGainRate = totalGain.divide(totalInvestment).toPercentage();

		return OverviewResponse.of(
			member.getNickname(),
			totalValuation,
			totalInvestment,
			totalGain,
			totalGainRate,
			totalAnnualDividend,
			totalAnnualDividendYield
		);
	}

	@Transactional(readOnly = true)
	public List<DashboardPieChartResponse> getPieChart(AuthMember authMember) {
		List<Portfolio> portfolios = portfolioRepository.findAllByMemberId(authMember.getMemberId());
		if (portfolios.isEmpty()) {
			return new ArrayList<>();
		}
		Money totalValuation = Money.zero();// 평가 금액 + 현금
		for (Portfolio portfolio : portfolios) {
			portfolio.applyCurrentPriceAllHoldingsBy(currentPriceManager);
			totalValuation = totalValuation.add(portfolio.calculateTotalAsset());
		}
		List<DashboardPieChartResponse> pieChartResponses = new ArrayList<>();
		for (Portfolio portfolio : portfolios) {
			pieChartResponses.add(DashboardPieChartResponse.of(portfolio, totalValuation));
		}
		// 정렬
		// 1. 가치(평가금액+현금) 기준 내림차순
		// 2. 총손익 기준 내림차순
		pieChartResponses.sort(
			((Comparator<DashboardPieChartResponse>)(o1, o2) -> o2.getValuation().compareTo(o1.getValuation()))
				.thenComparing((o1, o2) -> o2.getTotalGain().compareTo(o1.getTotalGain())));
		return pieChartResponses;
	}

	@Transactional(readOnly = true)
	public List<DashboardLineChartResponse> getLineChart(AuthMember authMember) {
		List<Portfolio> portfolios = portfolioRepository.findAllByMemberId(authMember.getMemberId());
		if (portfolios.isEmpty()) {
			return new ArrayList<>();
		}
		List<PortfolioGainHistory> portfolioGainHistories = new ArrayList<>();
		for (Portfolio portfolio : portfolios) {
			portfolioGainHistories.addAll(portfolioGainHistoryRepository.findAllByPortfolioId(portfolio.getId()));
		}
		Map<String, Money> timeValueMap = new HashMap<>();
		for (PortfolioGainHistory portfolioGainHistory : portfolioGainHistories) {
			String time = portfolioGainHistory.getCreateAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			timeValueMap.put(time, timeValueMap.getOrDefault(time, Money.zero())
				.add(portfolioGainHistory.getCash())
				.add(portfolioGainHistory.getCurrentValuation()));
		}
		return timeValueMap.keySet()
			.stream()
			.sorted()
			.map(key -> DashboardLineChartResponse.of(key, timeValueMap.get(key)))
			.collect(
				Collectors.toList());
	}
}
