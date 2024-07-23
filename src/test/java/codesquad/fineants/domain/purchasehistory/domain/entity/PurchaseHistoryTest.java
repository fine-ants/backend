package codesquad.fineants.domain.purchasehistory.domain.entity;

import static codesquad.fineants.domain.common.money.Currency.*;
import static org.assertj.core.api.Assertions.*;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Bank;
import codesquad.fineants.domain.common.money.Expression;
import codesquad.fineants.domain.common.money.Money;

class PurchaseHistoryTest {

	@DisplayName("매입 내역의 투자 금액을 구한다")
	@Test
	void calculateInvestmentAmount() {
		// given
		PurchaseHistory purchaseHistory = PurchaseHistory.now(
			Money.won(10000),
			Count.from(5),
			Strings.EMPTY,
			null
		);
		Bank bank = Bank.getInstance();
		// when
		Expression result = purchaseHistory.calculateInvestmentAmount();

		// then
		Money actual = bank.reduce(result, KRW);
		assertThat(actual).isEqualByComparingTo(Money.won(50000L));
	}

}
