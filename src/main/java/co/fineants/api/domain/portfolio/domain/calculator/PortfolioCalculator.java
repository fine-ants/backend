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

	public Expression calTotalGainRateBy(Portfolio portfolio) {
		return portfolio.calTotalGainRate(this);
	}

	/**
	 * 포트폴리오 총 손익율(TotalGainRate) 계산.
	 * <p>
	 * TotalGainRate = (TotalGain / TotalInvestment)
	 * </p>
	 * @param holdings 포트폴리오 종목 리스트
	 * @return 포트폴리오 총 손익율(TotalGainRate)
	 */
	public Expression calTotalGainRate(List<PortfolioHolding> holdings) {
		Expression totalGain = calTotalGain(holdings);
		Expression totalInvestment = calTotalInvestment(holdings);
		return totalGain.divide(totalInvestment);
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

	public Expression calBalanceBy(Portfolio portfolio) {
		return portfolio.calBalance(this);
	}

	/**
	 * 포트폴리오 잔고 계산
	 * <p>
	 * Balance = Budget - TotalInvestment
	 * </p>
	 * @param budget 예산
	 * @param totalInvestment 총 투자 금액
	 * @return 포트폴리오의 잔고
	 */
	public Expression calBalance(Expression budget, Expression totalInvestment) {
		return budget.minus(totalInvestment);
	}

	public Expression calTotalCurrentValuationBy(Portfolio portfolio) {
		return portfolio.calTotalCurrentValuation(this);
	}

	/**
	 * 포트폴리오 평가 금액 계산
	 * <p>
	 * TotalCurrentValuation = 각 종목들(holdings)의 평가 금액 합계
	 * </p>
	 * @param holdings 포트폴리오에 등록된 종목 리스트
	 * @return 포트폴리오 평가 금액
	 */
	public Expression calTotalCurrentValuation(List<PortfolioHolding> holdings) {
		return holdings.stream()
			.map(PortfolioHolding::calculateCurrentValuation)
			.reduce(Money.zero(), Expression::plus);
	}

	public Expression calTotalAssetBy(Portfolio portfolio) {
		return portfolio.calTotalAsset(this);
	}

	public Expression calTotalAsset(Expression balance, Expression totalCurrentValuation) {
		return balance.plus(totalCurrentValuation);
	}
}
