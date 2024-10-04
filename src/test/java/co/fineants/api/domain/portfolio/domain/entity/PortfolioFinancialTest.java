package co.fineants.api.domain.portfolio.domain.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.money.Money;

class PortfolioFinancialTest extends AbstractContainerBaseTest {

	@DisplayName("포트폴리오 금융 정보 객체를 생성한다")
	@Test
	void of_givenPortfolioFinancialInfo_whenCreatingInstance_thenReturnInstance() {
		// given
		Money budget = Money.won(1_000_000);
		Money targetGain = Money.won(1_500_000);
		Money maximumLoss = Money.won(900_000);
		// when
		PortfolioFinancial financial = PortfolioFinancial.of(budget, targetGain, maximumLoss);
		// then
		String expected = "(budget=1000000, targetGain=1500000, maximumLoss=900000)";
		Assertions.assertThat(financial.toString()).hasToString(expected);
	}
}
