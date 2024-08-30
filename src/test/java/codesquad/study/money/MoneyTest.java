package codesquad.study.money;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MoneyTest {
	@Test
	void testMultiplication() {
		Money five = Money.dollar(5);
		Expression product = five.times(2);
		assertThat(product).isEqualTo(Money.dollar(10));

		product = five.times(3);
		assertThat(product).isEqualTo(Money.dollar(15));
	}

	@Test
	void testFrancMultiplication() {
		Money five = Money.franc(5);
		assertThat(five.times(2)).isEqualTo(Money.franc(10));
		assertThat(five.times(3)).isEqualTo(Money.franc(15));
	}

	@Test
	void testEquality() {
		assertThat(Money.dollar(5).equals(Money.dollar(5))).isTrue();
		assertThat(Money.dollar(5).equals(Money.dollar(6))).isFalse();
		assertThat(Money.franc(5).equals(Money.franc(5))).isTrue();
		assertThat(Money.franc(5).equals(Money.franc(6))).isFalse();
		assertThat(Money.franc(5).equals(Money.dollar(5))).isFalse();
	}

	@Test
	void testDifferentClassEquality() {
		assertThat(new Money(10, "CHF").equals(new Money(10, "CHF"))).isTrue();
	}

	@Test
	void testSimpleAddition() {
		Money five = Money.dollar(5);
		Expression sum = five.plus(five);
		Bank bank = new Bank();
		Money reduced = bank.reduce(sum, "USD");
		assertThat(reduced).isEqualTo(Money.dollar(10));
	}

	@Test
	void testPlusSimpleAddition() {
		Money five = Money.dollar(5);
		Expression result = five.plus(five);
		Sum sum = (Sum)result;
		assertThat(five).isEqualTo(sum.augend);
	}

	@Test
	void testReduceSum() {
		Expression sum = new Sum(Money.dollar(3), Money.dollar(4));
		Bank bank = new Bank();
		Money result = bank.reduce(sum, "USD");
		assertThat(result).isEqualTo(Money.dollar(7));
	}

	@Test
	void testReduceMoney() {
		Bank bank = new Bank();
		Money result = bank.reduce(Money.dollar(1), "USD");
		assertThat(result).isEqualTo(Money.dollar(1));
	}

	@Test
	void testReduceMoneyDifferentCurrency() {
		Bank bank = new Bank();
		bank.addRate("CHF", "USD", 2);
		Money result = bank.reduce(Money.franc(2), "USD");
		assertThat(result).isEqualTo(Money.dollar(1));
	}

	@Test
	void testIdentityRate() {
		assertThat(new Bank().rate("USD", "USD")).isEqualTo(1);
	}

	@Test
	void testMixedAddition() {
		Expression fiveBucks = Money.dollar(5);
		Expression tenFrancs = Money.franc(10);
		Bank bank = new Bank();
		bank.addRate("CHF", "USD", 2);
		Money result = bank.reduce(fiveBucks.plus(tenFrancs), "USD");
		assertThat(result).isEqualTo(Money.dollar(10));
	}

	@Test
	void testSumPlusMoney() {
		Expression fiveBucks = Money.dollar(5);
		Expression tenFrancs = Money.franc(10);
		Bank bank = new Bank();
		bank.addRate("CHF", "USD", 2);
		Expression sum = new Sum(fiveBucks, tenFrancs).plus(fiveBucks);
		Money result = bank.reduce(sum, "USD");
		assertThat(result).isEqualTo(Money.dollar(15));
	}

	@Test
	void testSumTimes() {
		Expression fiveBucks = Money.dollar(5);
		Expression tenFrancs = Money.franc(10);
		Bank bank = new Bank();
		bank.addRate("CHF", "USD", 2);
		Expression sum = new Sum(fiveBucks, tenFrancs).times(2);
		Money result = bank.reduce(sum, "USD");
		assertThat(result).isEqualTo(Money.dollar(20));
	}
}
