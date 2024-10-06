package co.fineants.api.domain.portfolio.domain.calculator;

import java.util.List;

import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;

public class PortfolioCalculator {

	public Expression calTotalGainBy(Portfolio portfolio) {
		return portfolio.calTotalGain(this);
	}

	/**
	 * 포트폴리오 총 손익(TotalGain) 계산
	 * <p>
	 * TotalGain = 각 종목(holdings)의 총 손익(TotalGain) 합계
	 * </p>
	 *
	 * @param holdings 포트폴리오 종목 리스트
	 * @return 포트폴리오 총 손익 계산 합계
	 */
	public Expression calTotalGain(List<PortfolioHolding> holdings) {
		return holdings.stream()
			.map(PortfolioHolding::calculateTotalGain)
			.reduce(Money.zero(), Expression::plus);
	}

	public Expression calTotalInvestmentBy(Portfolio portfolio) {
		return portfolio.calTotalInvestment(this);
	}

	/**
	 * 포트폴리오 총 투자 금액 계산
	 * <p>
	 * TotalInvestment = 각 종목들(holdings)의 총 투자 금액 합계
	 * </p>
	 * @return 포트폴리오 총 투자 금액 합계
	 */
	public Expression calTotalInvestment(List<PortfolioHolding> holdings) {
		return holdings.stream()
			.map(PortfolioHolding::calculateTotalInvestmentAmount)
			.reduce(Money.wonZero(), Expression::plus);
	}
}
