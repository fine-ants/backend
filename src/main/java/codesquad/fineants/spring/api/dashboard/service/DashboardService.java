package codesquad.fineants.spring.api.dashboard.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistoryRepository;
import codesquad.fineants.spring.api.dashboard.response.DashboardLineChartResponse;
import codesquad.fineants.spring.api.dashboard.response.DashboardPieChartResponse;
import codesquad.fineants.spring.api.dashboard.response.OverviewResponse;
import codesquad.fineants.spring.api.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import lombok.RequiredArgsConstructor;

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
		Long totalValuation = 0L;// 평가 금액 + 현금?
		Long totalCurrentValuation = 0L; // 평가 금액
		Long totalInvestment = 0L; //총 주식에 투자된 돈
		Long totalGain = 0L; // 총 수익
		Long totalAnnualDividend = 0L; // 총 연간 배당금
		if (portfolios.isEmpty()) {
			return OverviewResponse.empty(member.getNickname());
		}
		for (Portfolio portfolio : portfolios) {
			portfolio.changeCurrentPriceFromHoldings(currentPriceManager);
			totalValuation += portfolio.calculateTotalAsset();
			totalCurrentValuation += portfolio.calculateTotalCurrentValuation();
			totalInvestment += portfolio.calculateTotalInvestmentAmount();
			totalGain += portfolio.calculateTotalGain();
			totalAnnualDividend += portfolio.calculateAnnualDividend();
		}
		Integer totalAnnualDividendYield = totalCurrentValuation != 0 ?
			(int)((totalAnnualDividend / totalCurrentValuation) * 100) : 0;
		Integer totalGainRate = totalInvestment != 0 ?
			(int)((totalGain.doubleValue() / totalInvestment.doubleValue()) * 100) : 0;

		return OverviewResponse.of(member.getNickname(), totalValuation, totalInvestment,
			totalGain, totalGainRate, totalAnnualDividend, totalAnnualDividendYield);
	}

	@Transactional(readOnly = true)
	public List<DashboardPieChartResponse> getPieChart(AuthMember authMember) {
		List<Portfolio> portfolios = portfolioRepository.findAllByMemberId(authMember.getMemberId());
		if (portfolios.isEmpty()) {
			return new ArrayList<>();
		}
		Long totalValuation = 0L;// 평가 금액 + 현금?
		for (Portfolio portfolio : portfolios) {
			portfolio.changeCurrentPriceFromHoldings(currentPriceManager);
			totalValuation += portfolio.calculateTotalAsset();
		}
		List<DashboardPieChartResponse> pieChartResponses = new ArrayList<>();
		for (Portfolio portfolio : portfolios) {
			pieChartResponses.add(DashboardPieChartResponse.of(portfolio, totalValuation));
		}
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
		Map<String, Long> timeValueMap = new HashMap<>();
		for (PortfolioGainHistory portfolioGainHistory : portfolioGainHistories) {
			String time = portfolioGainHistory.getCreateAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			timeValueMap.put(time, timeValueMap.getOrDefault(time, 0L) + portfolioGainHistory.getCash()
				+ portfolioGainHistory.getCurrentValuation());
		}
		return timeValueMap.keySet()
			.stream()
			.sorted()
			.map(key -> DashboardLineChartResponse.of(key, timeValueMap.get(key)))
			.collect(
				Collectors.toList());
	}
}
