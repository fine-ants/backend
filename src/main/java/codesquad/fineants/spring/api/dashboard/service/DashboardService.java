package codesquad.fineants.spring.api.dashboard.service;

import java.util.List;

import org.springframework.stereotype.Service;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
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

	public OverviewResponse getOverview(AuthMember authMember) {
		List<Portfolio> portfolios = portfolioRepository.findAllByMemberId(authMember.getMemberId());
		Member member = memberRepository.findById(authMember.getMemberId()).orElseThrow(() -> new BadRequestException(
			MemberErrorCode.NOT_FOUND_MEMBER));
		Long totalValuation = 0L;// 평가 금액 + 현금?
		Long totalCurrentValuation = 0L; // 평가 금액
		Long totalInvestment = 0L; //총 주식에 투자된 돈
		long totalGain = 0L; // 총 수익
		Long totalAnnualDividend = 0L; // 총 연간 배당금
		if (portfolios.isEmpty()) {
			return OverviewResponse.of(member.getNickname(), totalValuation, totalInvestment,
				0,
				totalAnnualDividend, 0);
		}
		for (Portfolio portfolio : portfolios) {
			portfolio.changeCurrentPriceFromHoldings(currentPriceManager);
			totalValuation += portfolio.calculateTotalAsset();
			totalCurrentValuation += portfolio.calculateTotalCurrentValuation();
			totalInvestment += portfolio.calculateTotalInvestmentAmount();
			totalGain += portfolio.calculateTotalGain();
			totalAnnualDividend += portfolio.calculateTotalAnnualDividend();
		}
		Integer totalAnnualDividendYield = (int)((totalAnnualDividend / totalCurrentValuation) * 100);
		Integer totalGainRate = (int)(((double)totalGain / (double)totalInvestment) * 100);

		return OverviewResponse.of(member.getNickname(), totalValuation, totalInvestment,
			totalGainRate,
			totalAnnualDividend, totalAnnualDividendYield);
	}
}
