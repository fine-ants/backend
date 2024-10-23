package co.fineants.api.domain.portfolio.domain.calculator;

import java.time.LocalDate;
import java.time.Month;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.common.money.RateDivision;
import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioPieChartItem;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.kis.repository.PriceRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.global.common.time.LocalDateTimeService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortfolioCalculator {

	private final PriceRepository currentPriceRepository;

	private static <T> Expression sumExpressions(List<T> data, Function<T, Expression> mapper) {
		return data.stream()
			.map(mapper)
			.reduce(Expression::plus)
			.orElseGet(Money::zero);
	}

	private static <T> Count countExpressions(List<T> data, Function<T, Count> mapper) {
		return data.stream()
			.map(mapper)
			.reduce(Count::add)
			.orElseGet(Count::zero);
	}

	/**
	 * 포트폴리오의 총 손익을 계산 후 반환.
	 *
	 * @param portfolio 포트폴리오 객체
	 * @return 포트폴리오의 총 손익
	 * @throws IllegalStateException 포트폴리오의 총 손익 계산이 실패하면 예외 발생
	 */
	public Expression calTotalGainBy(Portfolio portfolio) {
		try {
			return portfolio.calTotalGain(this);
		} catch (IllegalStateException e) {
			throw new IllegalStateException(
				String.format("Failed to calculate total gain for portfolio, portfolio:%s", portfolio), e);
		}
	}

	/**
	 * 포트폴리오의 총 손익율 계산 후 반환.
	 *
	 * @param portfolio 포트폴리오 객체
	 * @return 포트폴리오 총 손익율
	 * @throws IllegalStateException 포트폴리오의 총 손익율 계산이 실패하면 예외 발생
	 */
	public Expression calTotalGainRateBy(Portfolio portfolio) {
		try {
			return portfolio.calTotalGainRate(this);
		} catch (NoSuchElementException e) {
			throw new IllegalStateException(
				String.format("Failed to calculate totalGainRate for portfolio, portfolio:%s", portfolio), e);
		}
	}

	/**
	 * 포트폴리오의 총 투자 금액 계산 후 반환.
	 *
	 * @param portfolio 포트폴리오 객체
	 * @return 포트폴리오의 총 투자 금액
	 */
	public Expression calTotalInvestmentBy(Portfolio portfolio) {
		return portfolio.calTotalInvestment(this);
	}

	/**
	 * 포트폴리오 종목의 총 투자 금액 계산 후 반환.
	 *
	 * @param holding 포트폴리오 종목 객체
	 * @return 포트폴리오 종목의 총 투자 금액
	 */
	public Expression calTotalInvestmentBy(PortfolioHolding holding) {
		return holding.calTotalInvestment(this);
	}

	/**
	 * 포트폴리오 잔고 계산
	 * <p>
	 * Balance = Budget - TotalInvestment
	 * </p>
	 * @param portfolio 포트폴리오
	 * @return 포트폴리오의 잔고
	 */
	public Expression calBalanceBy(Portfolio portfolio) {
		return portfolio.calBalance(this);
	}

	/**
	 * 잔고 계산 후 반환
	 * <p>
	 * Balance = Budget - TotalInvestment
	 * </p>
	 * @param budget 에산
	 * @param totalInvestment 총 투자 금액
	 * @return 포트폴리오의 잔고
	 */
	public Expression calBalance(Expression budget, Expression totalInvestment) {
		return budget.minus(totalInvestment);
	}

	/**
	 * 포트폴리오의 총 평가금액 계산 후 반환.
	 *
	 * @param portfolio 포트폴리오 객체
	 * @return 포트폴리오의 총 평가 금액
	 * @throws IllegalStateException 포트폴리오의 총 평가금액 계산이 실패하면 예외 발생
	 */
	public Expression calTotalCurrentValuationBy(Portfolio portfolio) {
		try {
			return portfolio.calTotalCurrentValuation(this);
		} catch (NoSuchElementException e) {
			throw new IllegalStateException(
				String.format("Failed to calculate totalCurrentValuation for portfolio, portfolio:%s", portfolio), e);
		}
	}

	/**
	 * 포트폴리오 총 손익(TotalGain) 계산
	 * <p>
	 * TotalGain = 각 종목(holdings)의 총 손익(TotalGain) 합계
	 * </p>
	 *
	 * @param holdings 포트폴리오 종목 리스트
	 * @return 포트폴리오 총 손익 계산 합계
	 * @throws IllegalStateException 포트폴리오 종목(PortfolioHolding) 중 하나라도 계산에 실패하면 예외 발생
	 */
	public Expression calTotalGainBy(List<PortfolioHolding> holdings) {
		return sumExpressions(holdings, this::calTotalGainBy);
	}

	/**
	 * 포트폴리오 종목의 총 손익을 계산 후 반환.
	 * <p>
	 * TotalGain = (CurrentPrice - AverageCostPerShare) * NumShares
	 * </p>
	 * @param holding 포트폴리오 종목 객체
	 * @return 포트폴리오 종목의 총 손익
	 * @throws IllegalStateException 포트폴리오 종목의 총 손익 계산 실패시 예외 발생
	 */
	public Expression calTotalGainBy(PortfolioHolding holding) {
		Expression averageCostPerShare = calAverageCostPerShareBy(holding);
		int numShares = calNumSharesBy(holding).intValue();
		try {
			return this.calWithCurrentPriceBy(holding,
				currentPrice -> currentPrice.minus(averageCostPerShare).times(numShares));
		} catch (NoSuchElementException e) {
			throw new IllegalStateException(
				String.format("Failed to calculate totalGain for holding, holding:%s", holding), e);
		}
	}

	private Expression calWithCurrentPriceBy(PortfolioHolding holding, Function<Money, Expression> calFunction) {
		return currentPriceRepository.fetchPriceBy(holding)
			.map(calFunction)
			.orElseThrow(() -> new NoSuchElementException(
				String.format("No current price found for holding: %s, PriceRepository:%s", holding,
					currentPriceRepository)));
	}

	/**
	 * 포트폴리오 총 손익율(TotalGainRate) 계산.
	 * <p>
	 * TotalGainRate = (TotalGain / TotalInvestment)
	 * </p>
	 * @param holdings 포트폴리오 종목 리스트
	 * @return 포트폴리오 총 손익율(TotalGainRate)
	 * @throws NoSuchElementException 포트폴리오 종목(PortfolioHolding)에 따른 현재가가 저장소에 없으면 예외 발생
	 */
	public Expression calTotalGainRate(List<PortfolioHolding> holdings) {
		Expression totalGain = calTotalGainBy(holdings);
		Expression totalInvestment = calTotalInvestmentOfHolding(holdings);
		return totalGain.divide(totalInvestment);
	}

	/**
	 * 포트폴리오 총 투자 금액 계산
	 * <p>
	 * TotalInvestment = 각 종목들(holdings)의 총 투자 금액 합계
	 * </p>
	 * @param holdings the holdings
	 * @return 포트폴리오 총 투자 금액 합계
	 */
	public Expression calTotalInvestmentOfHolding(List<PortfolioHolding> holdings) {
		return sumExpressions(holdings, this::calTotalInvestmentBy);
	}

	/**
	 * 포트폴리오 평가 금액 계산
	 * <p>
	 * TotalCurrentValuation = 각 종목들(holdings)의 평가 금액 합계
	 * </p>
	 * @param holdings 포트폴리오에 등록된 종목 리스트
	 * @return 포트폴리오 평가 금액
	 * @throws IllegalStateException 포트폴리오 평가 금액 계산이 실패하면 예외 발생
	 */
	public Expression calTotalCurrentValuation(List<PortfolioHolding> holdings) {
		return sumExpressions(holdings, this::calTotalCurrentValuationBy);
	}

	/**
	 * 포트폴리오 종목의 총 평가 금액 계산 후 반환.
	 * <p>
	 * CurrentValuation = CurrentPrice * NumShares
	 * </p>
	 * @param holding 포트폴리오 종목 객체
	 * @return 포트폴리오 종목의 총 평가 금액
	 * @throws IllegalStateException 포트폴리오 종목의 총 평가 금액 계산 실패시 예외 발생
	 */
	public Expression calTotalCurrentValuationBy(PortfolioHolding holding) {
		try {
			int numShares = this.calNumSharesBy(holding).intValue();
			return this.calWithCurrentPriceBy(holding, currentPrice -> currentPrice.times(numShares));
		} catch (NoSuchElementException e) {
			throw new IllegalStateException(
				String.format("Failed to calculate totalCurrentValuation for holding, holding:%s", holding), e);
		}
	}

	/**
	 * 포트폴리오의 총 자산 계산 후 반환.
	 * @param portfolio 포트폴리오 객체
	 * @return 포트폴리오 총 자산
	 */
	public Expression calTotalAssetBy(Portfolio portfolio) {
		return portfolio.calTotalAsset(this);
	}

	/**
	 * 총 자산을 계산 후 반환
	 *
	 * @param balance 잔고
	 * @param totalCurrentValuation 총 평가 금액
	 * @return 총 자산
	 */
	public Expression calTotalAsset(Expression balance, Expression totalCurrentValuation) {
		return balance.plus(totalCurrentValuation);
	}

	/**
	 * 포트폴리오 당일 손익을 계산하여 반환.
	 *
	 * <p>
	 * dailyGain = totalCurrentValuation - previousCurrentValuation
	 * 이전일의 포트포릴오 내역의 각 종목들의 평가 금액이 없는 경우 총 투자금액으로 뺀다
	 * </p>
	 * @param history 이전 포트폴리오 내역
	 * @param portfolio the portfolio
	 * @return 포트폴리오 당일 손익
	 */
	public Expression calDailyGain(PortfolioGainHistory history, Portfolio portfolio) {
		Expression previousCurrentValuation = history.getCurrentValuation();
		Money previousCurrentValuationMoney = Bank.getInstance().toWon(previousCurrentValuation);
		Expression totalCurrentValuation = calTotalCurrentValuationBy(portfolio);
		if (previousCurrentValuationMoney.hasZero()) {
			Expression totalInvestment = calTotalInvestmentBy(portfolio);
			return totalCurrentValuation.minus(totalInvestment);
		}
		return totalCurrentValuation.minus(previousCurrentValuation);
	}

	/**
	 * 포트폴리오의 당일 손익율을 계산하여 반환.
	 *
	 * <p>
	 * 이전 포트폴리오 내역이 없는 경우
	 * dailyGainRate = (totalCurrentValuation - totalInvestment) / totalInvestment
	 * 이전 포트폴리오 내역이 있는 경우
	 * dailyGainRate = (totalCurrentValuation - previousCurrentValuation) / previousCurrentValuation
	 * </p>
	 *
	 * @param history 이전 포트폴리오 내역
	 * @param portfolio 포트폴리오 객체
	 * @return 포트폴리오의 당일 손익율
	 */
	public Expression calDailyGainRateBy(PortfolioGainHistory history, Portfolio portfolio) {
		Money prevCurrentValuation = history.getCurrentValuation();
		Expression totalCurrentValuation = calTotalCurrentValuationBy(portfolio);
		if (prevCurrentValuation.hasZero()) {
			Expression totalInvestment = calTotalInvestmentBy(portfolio);
			return totalCurrentValuation.minus(totalInvestment)
				.divide(totalInvestment);
		}
		return totalCurrentValuation.minus(prevCurrentValuation)
			.divide(prevCurrentValuation);
	}

	/**
	 * 포트폴리오의 이번달 배당금 합계를 계산 후 반환.
	 *
	 * @param portfolio 포트폴리오
	 * @return 배당금 합계
	 */
	public Expression calCurrentMonthDividendBy(Portfolio portfolio) {
		return portfolio.calCurrentMonthDividend(this);
	}

	/**
	 * 포트폴리오의 당월 예상 배당금 계산후 반환한다.
	 * <p>
	 * 당월 예상 배당금 = 각 종목들(holdings)의 해당월의 배당금 합계
	 * </p>
	 * @param holdings 포트폴리오 종목 리스트
	 * @return 포트폴리오의 당월 예상 배당금 합계
	 */
	public Expression calCurrentMonthDividendBy(List<PortfolioHolding> holdings) {
		return sumExpressions(holdings, holding -> holding.calCurrentMonthDividend(this));
	}

	/**
	 * 포트폴리오 종목의 이번달 배당금 계산 후 반환
	 * <p>
	 * CurrentMonthDividend = sum(PurchaseHistory.NumShares * StockDividend)
	 * </p>
	 * @param stock the stock
	 * @param histories the histories
	 * @return 이번달 배당금
	 */
	public Expression calCurrentMonthExpectedDividend(Stock stock, List<PurchaseHistory> histories) {
		return stock.getCurrentMonthDividends().stream()
			.map(stockDividend -> histories.stream()
				.filter(stockDividend::isPurchaseDateBeforeExDividendDate)
				.map(PurchaseHistory::getNumShares)
				.reduce(Count.zero(), Count::add)
				.multiply(stockDividend.getDividend()))
			.reduce(Expression::plus)
			.orElseGet(Money::zero);
	}

	/**
	 * Cal annual dividend by expression.
	 *
	 * @param dateTimeService the date time service
	 * @param portfolio the portfolio
	 * @return the expression
	 */
	public Expression calAnnualDividendBy(LocalDateTimeService dateTimeService, Portfolio portfolio) {
		return portfolio.calAnnualDividend(dateTimeService, this);
	}

	/**
	 * 포트폴리오의 총 연간 배당금 계산 후 반환한다.
	 *
	 * <p>
	 * AnnualDividend = 각 종목들(holdings)의 연간 배당금 합계
	 * </p>
	 * @param dateTimeService 시간 서비스
	 * @param holdings 포트폴리오의 종목 리스트
	 * @return 포트폴리오의 총 연간 배당금
	 */
	public Expression calAnnualDividend(LocalDateTimeService dateTimeService, List<PortfolioHolding> holdings) {
		return holdings.stream()
			.map(holding -> this.calMonthlyDividendMapBy(holding, dateTimeService.getLocalDateWithNow()))
			.map(map -> map.values().stream()
				.reduce(Expression::plus)
				.orElseGet(Money::zero))
			.reduce(Expression::plus)
			.orElseGet(Money::zero);
	}

	/**
	 * 포트폴리오의 총 연간 배당율을 계산 후 반환한다.
	 *
	 * <p>
	 * AnnualDividendYield = (TotalAnnualDividend / TotalCurrentValuation)
	 * </p>
	 * @param localDateTimeService 시간 서비스
	 * @param portfolio 포트폴리오 객체
	 * @return 포트폴리오의 총 연간 배당율
	 */
	public Expression calAnnualDividendYieldBy(LocalDateTimeService localDateTimeService, Portfolio portfolio) {
		Expression totalAnnualDividend = calAnnualDividendBy(localDateTimeService, portfolio);
		Expression totalCurrentValuation = calTotalCurrentValuationBy(portfolio);
		return totalAnnualDividend.divide(totalCurrentValuation);
	}

	/**
	 * 포트폴리오의 투자 대비 연간 배당금
	 *
	 * @param localDateTimeService 시간 서비스
	 * @param portfolio 포트폴리오
	 * @return 투자 대비 연간 배당금
	 */
	public Expression calAnnualInvestmentDividendYieldBy(LocalDateTimeService localDateTimeService,
		Portfolio portfolio) {
		return portfolio.calAnnualInvestmentDividendYield(localDateTimeService, this);
	}

	/**
	 * 포트폴리오의 투자 대비 연간 배당율 계산 후 반환한다.
	 * <p>
	 * AnnualInvestmentDividendYield = AnnualDividend / TotalInvestment
	 * </p>
	 * @param annualDividend 포트폴리오 연간 배당금
	 * @param totalInvestment 포트폴리오 총 투자 금액
	 * @return 포트폴리오의 투자 대비 연간 배당율
	 */
	public Expression calAnnualInvestmentDividendYield(Expression annualDividend, Expression totalInvestment) {
		return annualDividend.divide(totalInvestment);
	}

	/**
	 * 포트폴리오의 최대손실율 계산 후 반환.
	 * @param portfolio 포트폴리오 객체
	 * @return 포트폴리오의 최대손실율
	 */
	public Expression calMaximumLossRateBy(Portfolio portfolio) {
		return portfolio.calMaximumLossRate(this);
	}

	/**
	 * 포트폴리오의 최대손실율 계산 후 반환.
	 * <p>
	 * MaximumLossRate = ((Budget - MaximumLoss) / Budget)
	 * </p>
	 * @param budget 예산
	 * @param maximumLoss 최대손실금액
	 * @return 포트폴리오의 최대손실율
	 */
	public RateDivision calMaximumLossRate(Money budget, Money maximumLoss) {
		return budget.minus(maximumLoss).divide(budget);
	}

	/**
	 * 포트폴리오의 목표수익금액율 계산 후 반환.
	 * @param portfolio 포트폴리오 객체
	 * @return 포트폴리오의 목표수익금액율
	 */
	public Expression calTargetGainRateBy(Portfolio portfolio) {
		return portfolio.calTargetGainRate(this);
	}

	/**
	 * 포트폴리오의 목표수익금액율 계산 후 반환.
	 * <p>
	 * TargetGainRate = ((TargetGain - Budget) / Budget)
	 * </p>
	 * @param budget 예산
	 * @param targetGain 목표수익금액
	 * @return 포트폴리오의 목표수익금액율
	 */
	public RateDivision calTargetGainRate(Expression budget, Expression targetGain) {
		return targetGain.minus(budget).divide(budget);
	}

	/**
	 * 포트폴리오의 현금 비중을 계산 후 반환.
	 * <p>
	 * CashWeight = Balance / TotalAsset
	 * </p>
	 * @param portfolio 포트폴리오 객체
	 * @return 포트폴리오의 현금 비중
	 */
	public RateDivision calCashWeightBy(Portfolio portfolio) {
		Expression balance = calBalanceBy(portfolio);
		Expression totalAsset = calTotalAssetBy(portfolio);
		return balance.divide(totalAsset);
	}

	/**
	 * 포트폴리오 종목 비중 계산 후 반환
	 * <p>
	 * CurrentValuationWeight = CurrentValuation / TotalAsset
	 * </p>
	 * @param holding 포트폴리오 종목 객체
	 * @param totalAsset 포트폴리오 총 자산
	 * @return 포트폴리오 종목 비중
	 */
	public RateDivision calCurrentValuationWeightBy(PortfolioHolding holding, Expression totalAsset) {
		return this.calTotalCurrentValuationBy(holding).divide(totalAsset);
	}

	/**
	 * 포트폴리오의 섹터 맵 계산 후 반환.
	 *
	 * @param portfolio 포트폴리오
	 * @return 섹터 맵
	 */
	public Map<String, List<Expression>> calSectorChartBy(Portfolio portfolio) {
		return portfolio.createSectorChart(this);
	}

	/**
	 * 포트폴리오 종목들의 섹터 맵 계산 후 반환.
	 *
	 * @param holdings 포트폴리오 종목 리스트
	 * @param balance 잔고
	 * @return 섹터별 총 평가금액 맵
	 */
	public Map<String, List<Expression>> calSectorChart(List<PortfolioHolding> holdings, Expression balance) {
		Map<String, List<Expression>> sector = holdings.stream()
			.collect(Collectors.groupingBy(portfolioHolding -> portfolioHolding.getStock().getSector(),
				Collectors.mapping(
					this::calTotalCurrentValuationBy,
					Collectors.toList())));
		sector.put("현금", List.of(balance));
		return sector;
	}

	/**
	 * 총 자산 대비 평가 금액 비중을 계산 후 반환.
	 * <p>
	 * CurrentValuationWeight = CurrentValuation / TotalAsset
	 * </p>
	 * @param currentValuation 평가 금액
	 * @param totalAsset 총 자산
	 * @return 평가 금액 비중
	 */
	public RateDivision calCurrentValuationWeight(Expression currentValuation, Expression totalAsset) {
		return currentValuation.divide(totalAsset);
	}

	/**
	 * 포트폴리오가 목표 수익금액에 도달했는지 여부 검사.
	 *
	 * @param portfolio 포트폴리오
	 * @return 도달 여부
	 */
	public boolean reachedTargetGainBy(Portfolio portfolio) {
		return portfolio.reachedTargetGain(this);
	}

	/**
	 * 포트폴리오가 최대 손실금액에 도달했는지 여부 검사.
	 *
	 * @param portfolio 포트폴리오
	 * @return 도달 여부
	 */
	public boolean reachedMaximumLossBy(Portfolio portfolio) {
		return portfolio.reachedMaximumLoss(this);
	}

	/**
	 * 포트폴리오의 평가 금액의 종목 비중(현금 포함) 계산 후 파이 차트 리스트로 반환한다.
	 * <p>
	 * 각 파이 차트의 요소의 정렬 기준은 다음과 같습니다.
	 * 1. 평가금액(CurrentValuation) 내림차순
	 * 2. 총손익(TotalGain) 내림차순
	 * </p>
	 * @param portfolio 포트폴리오 객체
	 * @return 파이 차트 요소 리스트
	 */
	public List<PortfolioPieChartItem> calPieChartItemBy(Portfolio portfolio) {
		Bank bank = Bank.getInstance();
		Money balance = bank.toWon(calBalanceBy(portfolio));
		Percentage weight = calCashWeightBy(portfolio).toPercentage(bank, Currency.KRW);
		PortfolioPieChartItem cash = PortfolioPieChartItem.cash(weight, balance);

		return Stream.concat(portfolio.calCurrentValuationWeights(this).stream(), Stream.of(cash))
			.sorted(Comparator.comparing(PortfolioPieChartItem::getValuation, Comparator.reverseOrder())
				.thenComparing(PortfolioPieChartItem::getTotalGain, Comparator.reverseOrder()))
			.toList();
	}

	/**
	 * 포트폴리오의 월별 배당금액 합계 계산 후 반환.
	 *
	 * @param portfolio 포트폴리오
	 * @param currentLocalDate 기준이 될 현재 일자
	 * @return 월별 배당금액 합계 맵
	 */
	public Map<Month, Expression> calTotalDividendBy(Portfolio portfolio, LocalDate currentLocalDate) {
		return portfolio.calTotalDividend(this, currentLocalDate);
	}

	/**
	 * 월별 배당금 합계 계산 후 반환.
	 *
	 * @param holdings 포트폴리오 종목 리스트
	 * @param currentLocalDate 기준이 될 현재 일자
	 * @return 월별 배당금액 합계 맵
	 */
	public Map<Month, Expression> calTotalDividend(List<PortfolioHolding> holdings, LocalDate currentLocalDate) {
		return holdings.stream()
			.flatMap(holding -> this.calMonthlyDividendMapBy(holding, currentLocalDate).entrySet().stream())
			.collect(Collectors.groupingBy(Map.Entry::getKey,
				Collectors.reducing(Money.zero(), Map.Entry::getValue, Expression::plus))
			);
	}

	/**
	 * 포트폴리오 종목의 예상 연간 배당율을 계산 후 반환.
	 * <p>
	 * AnnualExpectedDividendYield = (AnnualExpectedDividend / CurrentValuation)
	 * </p>
	 * @param holding 포트폴리오 종목 객체
	 * @return 예상 연간 배당율
	 */
	public Expression calAnnualExpectedDividendYieldBy(PortfolioHolding holding) {
		Expression annualDividend = this.calAnnualExpectedDividendBy(holding);
		Expression currentValuation = this.calTotalCurrentValuationBy(holding);
		return annualDividend.divide(currentValuation);
	}

	/**
	 * 포트폴리오 종목의 총 손익 비중 계산후 반환.
	 *
	 * @param holding 포트폴리오 종목
	 * @return 총 손익 비중
	 */
	public Percentage calTotalGainPercentage(PortfolioHolding holding) {
		Bank bank = Bank.getInstance();
		Expression totalGain = calTotalGainBy(holding);
		Expression totalInvestment = calTotalInvestmentBy(holding);
		return totalGain.divide(totalInvestment).toPercentage(bank, Currency.KRW);
	}

	/**
	 * 포트폴리오의 당일 변동 금액 계산 후 반환.
	 *
	 * @param holding 포트폴리오 종목
	 * @param closingPrice 종가
	 * @return 당일 변동 금액
	 */
	public Expression calDailyChange(@NotNull PortfolioHolding holding, @NotNull Expression closingPrice) {
		return this.calWithCurrentPriceBy(holding, currentPrice -> currentPrice.minus(closingPrice));
	}

	/**
	 * 포트폴리오 종목의 당일 변동율 계산 후 반환.
	 * <p>
	 * DailyChangeRate = ((CurrentPrice - ClosingPrice) / ClosingPrice)
	 * </p>
	 * @param holding 포트폴리오 종목
	 * @param closingPrice 종목 종가
	 * @return 당일 변동율
	 */
	public Expression calDailyChangeRate(@NotNull PortfolioHolding holding, @NotNull Expression closingPrice) {
		return this.calWithCurrentPriceBy(holding,
			currentPrice -> currentPrice.minus(closingPrice).divide(closingPrice));
	}

	/**
	 * 포트폴리오 종목의 현재가를 가져온 후 반환.
	 *
	 * @param holding 포트폴리오 종목
	 * @return 현재가
	 */
	public Expression fetchCurrentPrice(PortfolioHolding holding) {
		return this.calWithCurrentPriceBy(holding, currentPrice -> currentPrice);
	}

	/**
	 * 포트폴리오 종목의 예상되는 배당금액 계산 후 반환.
	 *
	 * @param holding 포트폴리오 종목
	 * @return 연간 배당금액 합계
	 */
	public Expression calAnnualExpectedDividendBy(PortfolioHolding holding) {
		return holding.calAnnualExpectedDividend(this);
	}

	/**
	 * 포트폴리오 종목의 평균 매입가를 계산 후 반환.
	 *
	 * @param holding 포트폴리오 종목 객체
	 * @return 평균 매입가
	 */
	public Expression calAverageCostPerShareBy(PortfolioHolding holding) {
		return holding.calAverageCostPerShare(this);
	}

	/**
	 * 매입 이력들의 평균 매입가를 계산 후 반환.
	 * <p>
	 * AverageCostPerShare = TotalInvestment / NumShares
	 * </p>
	 * @param histories 매입 이력 리스트
	 * @return 평균 매입가
	 */
	public Expression calAverageCostPerShare(List<PurchaseHistory> histories) {
		Expression totalInvestment = this.calTotalInvestmentOfPurchaseHistories(histories);
		Count numShares = this.calNumShares(histories);
		return totalInvestment.divide(numShares);
	}

	private Expression calTotalInvestmentOfPurchaseHistories(List<PurchaseHistory> histories) {
		return sumExpressions(histories, PurchaseHistory::calInvestmentAmount);
	}

	/**
	 * 포트폴리오 종목 매입 개수 계산 후 반환.
	 * <p>
	 * NumShares = sum(PurchaseHistory.NumShares)
	 * </p>
	 * @param histories 매입 이력 리스트
	 * @return 종목 매입 합계
	 */
	public Count calNumShares(List<PurchaseHistory> histories) {
		return countExpressions(histories, PurchaseHistory::getNumShares);
	}

	/**
	 * Cal num shares by count.
	 *
	 * @param holding the holding
	 * @return the count
	 */
	public Count calNumSharesBy(PortfolioHolding holding) {
		return holding.calNumShares(this);
	}

	/**
	 * 포트폴리오 종목의 총 투자금액 계산 후 반환.
	 * <p>
	 * TotalInvestmentAmount = sum(PurchaseHistory.InvestmentAmount)
	 * </p>
	 * @param histories 매입 이력 리스트
	 * @return 총 투자금액 합계
	 */
	public Expression calTotalInvestment(List<PurchaseHistory> histories) {
		return sumExpressions(histories, PurchaseHistory::calInvestmentAmount);
	}

	/**
	 * 포트폴리오 종목의 예상 연배당금 계산 후 반환.
	 * <p>
	 * AnnualExpectedDividend = AnnualDividend + AnnualExpectedDividend
	 * </p>
	 * <p>
	 * 예상 연배당금(AnnualExpectedDividend)에는 실제 연배당금(AnnualDividend)이 포함되어 있습니다.
	 * </p>
	 * @param stock the stock
	 * @param histories the histories
	 * @return 예상 연배당금
	 */
	public Expression calAnnualExpectedDividend(Stock stock, List<PurchaseHistory> histories) {
		Expression annualDividend = this.calAnnualDividend(stock, histories);
		Expression annualExpectedDividend = stock.createMonthlyExpectedDividends(histories, LocalDate.now())
			.values()
			.stream()
			.reduce(Expression::plus)
			.orElseGet(Money::zero);
		return annualDividend.plus(annualExpectedDividend);
	}

	private Expression calAnnualDividend(Stock stock, List<PurchaseHistory> histories) {
		return histories.stream()
			.flatMap(history -> stock.getCurrentYearDividends().stream()
				.filter(stockDividend -> stockDividend.isSatisfiedBy(history))
				.map(stockDividend -> stockDividend.calculateDividendSum(history.getNumShares())))
			.reduce(Expression::plus)
			.orElseGet(Money::zero);
	}

	/**
	 * 포트폴리오 종목의 월별 배당금액 계산 후 반환
	 *
	 * @param holding 포트폴리오 종목
	 * @param currentLocalDate 기준이 될 현재 일자
	 * @return 월별 배당금액
	 */
	public Map<Month, Expression> calMonthlyDividendMapBy(PortfolioHolding holding, LocalDate currentLocalDate) {
		return holding.calMonthlyDividendMap(this, currentLocalDate);
	}

	/**
	 * 포트폴리오 종목의 월별 배당금 합계를 가진 맵을 생성 후 반환.
	 * <p>
	 * 결과 맵의 배당금 합계에는 실제 월별 배당금과 예상되는 월별 배당금 합계가 포함되어 있습니다.
	 * </p>
	 *
	 * @param stock 종목 객체
	 * @param purchaseHistories 매입 이력 리스트
	 * @param currentLocalDate 기준 일자
	 * @return 월별 배당금 합계 맵
	 */
	public Map<Month, Expression> calMonthlyDividendMap(Stock stock, List<PurchaseHistory> purchaseHistories,
		LocalDate currentLocalDate) {
		Map<Month, Expression> result = new EnumMap<>(Month.class);
		Map<Month, Expression> monthlyDividends = stock.createMonthlyDividends(purchaseHistories, currentLocalDate);
		Map<Month, Expression> monthlyExpectedDividends = stock.createMonthlyExpectedDividends(purchaseHistories,
			currentLocalDate);
		monthlyDividends.forEach(
			(month, dividend) -> result.merge(month, dividend, Expression::plus));
		monthlyExpectedDividends.forEach(
			(month, dividend) -> result.merge(month, dividend, Expression::plus));
		return result;
	}

	/**
	 * 포트폴리오 종목의 파이차트 요소를 생성후 반환.
	 * @param holding 포트폴리오 종목 객체
	 * @param totalAsset 포트폴리오 총 자산
	 * @return 파이차트 요소
	 */
	public PortfolioPieChartItem calPortfolioPieChartItemBy(PortfolioHolding holding, Expression totalAsset) {
		String name = holding.getCompanyName();
		Percentage weightPercentage = this.calCurrentValuationWeightBy(holding, totalAsset)
			.toPercentage(Bank.getInstance(), Currency.KRW);
		Expression currentValuation = this.calTotalCurrentValuationBy(holding);
		Expression totalGain = this.calTotalGainBy(holding);
		Percentage totalGainRate = this.calTotalGainPercentage(holding);
		return PortfolioPieChartItem.stock(name, currentValuation, weightPercentage, totalGain, totalGainRate);
	}
}
