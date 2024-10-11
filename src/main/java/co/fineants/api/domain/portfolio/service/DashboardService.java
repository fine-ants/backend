package co.fineants.api.domain.portfolio.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.RateDivision;
import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.gainhistory.repository.PortfolioGainHistoryRepository;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.dto.response.DashboardLineChartResponse;
import co.fineants.api.domain.portfolio.domain.dto.response.DashboardPieChartResponse;
import co.fineants.api.domain.portfolio.domain.dto.response.OverviewResponse;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.global.errors.errorcode.MemberErrorCode;
import co.fineants.api.global.errors.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {
	private final PortfolioRepository portfolioRepository;
	private final MemberRepository memberRepository;
	private final CurrentPriceRedisRepository currentPriceRedisRepository;
	private final PortfolioGainHistoryRepository portfolioGainHistoryRepository;
	private final LocalDateTimeService localDateTimeService;
	private final PortfolioCalculator calculator;

	@Transactional(readOnly = true)
	@Secured("ROLE_USER")
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
			Expression totalAsset = calculator.calTotalAssetBy(portfolio);
			totalValuation = totalValuation.plus(totalAsset);
			totalCurrentValuation = totalCurrentValuation.plus(calculator.calTotalCurrentValuationBy(portfolio));
			totalInvestment = totalInvestment.plus(calculator.calTotalInvestmentBy(portfolio));
			totalGain = totalGain.plus(calculator.calTotalGainBy(portfolio));
			totalAnnualDividend = totalAnnualDividend.plus(
				calculator.calAnnualDividendBy(localDateTimeService, portfolio));
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
	@Secured("ROLE_USER")
	public List<DashboardPieChartResponse> getPieChart(Long memberId) {
		List<Portfolio> portfolios = portfolioRepository.findAllByMemberId(memberId);
		if (portfolios.isEmpty()) {
			return new ArrayList<>();
		}
		Expression totalValuation = Money.wonZero(); // 평가 금액 + 현금
		for (Portfolio portfolio : portfolios) {
			Expression totalAsset = calculator.calTotalAssetBy(portfolio);
			totalValuation = totalValuation.plus(totalAsset);
		}
		List<DashboardPieChartResponse> pieChartResponses = new ArrayList<>();
		for (Portfolio portfolio : portfolios) {
			pieChartResponses.add(DashboardPieChartResponse.of(portfolio, totalValuation, calculator));
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
	@Secured("ROLE_USER")
	@Cacheable(value = "lineChartCache", key = "#memberId")
	public List<DashboardLineChartResponse> getLineChart(Long memberId) {
		List<PortfolioGainHistory> histories = portfolioRepository.findAllByMemberId(memberId).stream()
			.map(Portfolio::getId)
			.map(portfolioGainHistoryRepository::findAllByPortfolioId)
			.flatMap(Collection::stream)
			.toList();

		Map<String, Expression> result = histories.stream()
			.collect(Collectors.toMap(
				PortfolioGainHistory::getLineChartKey,
				PortfolioGainHistory::calculateTotalPortfolioValue,
				Expression::plus
			));
		return result.keySet()
			.stream()
			.sorted()
			.map(key -> DashboardLineChartResponse.of(key, result.get(key)))
			.toList();
	}
}
