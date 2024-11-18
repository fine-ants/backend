package co.fineants.api.domain.portfolio.domain.entity;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.money.Money;

class PortfolioFinancialTest extends AbstractContainerBaseTest {

	@DisplayName("예산, 목표수익금액, 최대손실금액이 주어지고 포트폴리오 금융 정보 객체를 생성한다")
	@ParameterizedTest(name = "[{index}] budget:{0}, targetGain:{1}, maximumLoss:{2}")
	@MethodSource(value = "portfolioFinancialInfo")
	void of_givenPortfolioFinancialInfo_whenCreatingInstance_thenReturnInstance(int budget, int targetGain,
		int maximumLoss) {
		// given
		Money budgetMoney = Money.won(budget);
		Money targetGainMoney = Money.won(targetGain);
		Money maximumLossMoney = Money.won(maximumLoss);
		// when
		PortfolioFinancial financial = PortfolioFinancial.of(budgetMoney, targetGainMoney, maximumLossMoney);
		// then
		String expected = String.format("(budget=%s, targetGain=%s, maximumLoss=%s)", budgetMoney, targetGainMoney,
			maximumLossMoney);
		Assertions.assertThat(financial.toString()).hasToString(expected);
	}

	private static Stream<Arguments> portfolioFinancialInfo() {
		return Stream.of(
			Arguments.of(1_000_000, 1_500_000, 900_000),
			Arguments.of(0, 0, 0),
			Arguments.of(0, 1_500_000, 900_000),
			Arguments.of(1_000_000, 0, 0),
			Arguments.of(1_000_000, 1_500_000, 0),
			Arguments.of(1_000_000, 0, 900_000)
		);
	}

	@DisplayName("유효하지 않은 예산, 목표수익금액, 최대손실금액 조합이 주어지고 포트폴리오 금융 정보 객체 생성을 실패한다")
	@ParameterizedTest(name = "[{index}] budget:{0}, targetGain:{1}, maximumLoss:{2}")
	@MethodSource(value = "invalidPortfolioFinancialInfo")
	void of_givenInvalidPortfolioFinancialInfo_whenCreatingInstance_thenThrowException(int budget, int targetGain,
		int maximumLoss) {
		// given
		Money budgetMoney = Money.won(budget);
		Money targetGainMoney = Money.won(targetGain);
		Money maximumLossMoney = Money.won(maximumLoss);
		// when
		Throwable throwable = Assertions.catchThrowable(() ->
			PortfolioFinancial.of(budgetMoney, targetGainMoney, maximumLossMoney));
		// then
		Assertions.assertThat(throwable)
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("invalid PortfolioFinancial budget: %s, targetGain: %s, maximumLoss: %s", budgetMoney,
				targetGainMoney, maximumLossMoney);
	}

	public static Stream<Arguments> invalidPortfolioFinancialInfo() {
		return Stream.of(
			Arguments.of(1_000_000, 900_000, 900_000, "목표수익금액은 예산보다 작거나 같으면 안된다"),
			Arguments.of(1_000_000, 1_500_000, 1_500_000, "최대손실금액은 예산보다 크거나 같으면 안된다"),
			Arguments.of(-1_000_000, 1_500_000, 900_000, "예산은 음수이면 안된다"),
			Arguments.of(1_000_000, -1_500_000, 900_000, "목표수익금액은 음수이면 안된다"),
			Arguments.of(1_000_000, 1_500_000, -900_000, "최대손실금액은 음수이면 안된다")
		);
	}
}
