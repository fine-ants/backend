package codesquad.fineants.domain.common.money;

import static codesquad.fineants.domain.common.money.Currency.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import codesquad.fineants.domain.common.count.Count;

class MoneyTest {

	@DisplayName("달러 곱셈")
	@Test
	void testMultiplication() {
		Money five = Money.dollar(5);

		assertEquals(Money.dollar(10), five.times(2));
		assertEquals(Money.dollar(15), five.times(3));
	}

	@DisplayName("머니 동치성")
	@Test
	void testEquality() {
		assertEquals(Money.dollar(5), Money.dollar(5));
		assertNotEquals(Money.dollar(5), Money.dollar(6));
		assertEquals(Money.franc(5), Money.franc(5));
		assertNotEquals(Money.franc(5), Money.franc(6));
		assertNotEquals(Money.franc(5), Money.dollar(5));
	}

	@DisplayName("프랑 곱셉")
	@Test
	void testFrancMultiplication() {
		Money five = Money.franc(5);
		assertEquals(Money.franc(10), five.times(2));
		assertEquals(Money.franc(15), five.times(3));
	}

	@DisplayName("화폐 통화 확인")
	@Test
	void testCurrency() {
		assertEquals(USD, Money.dollar(1).currency());
		assertEquals(CHF, Money.franc(1).currency());
	}

	@DisplayName("달러로 전환")
	@Test
	void testReduceSum() {
		Expression sum = new Sum(Money.dollar(3), Money.dollar(4));
		Bank bank = new Bank();
		Money result = bank.reduce(sum, USD);
		assertEquals(Money.dollar(7), result);
	}

	@DisplayName("화폐 더하기")
	@Test
	void testSimpleAddition() {
		Money five = Money.dollar(5);
		Expression sum = five.plus(five);
		Bank bank = new Bank();
		Money reduced = bank.reduce(sum, USD);
		assertEquals(Money.dollar(10), reduced);
	}

	@DisplayName("달러를 달러로 변환")
	@Test
	void testReduceMoney() {
		Bank bank = new Bank();
		Money result = bank.reduce(Money.dollar(1), USD);
		assertEquals(Money.dollar(1), result);
	}

	@DisplayName("프랑을 달러로 변환")
	@Test
	void testReduceMoneyDifferentCurrency() {
		Bank bank = new Bank();
		bank.addRate(CHF, USD, 2);
		Money result = bank.reduce(Money.franc(2), USD);
		assertEquals(Money.dollar(1), result);
	}

	@DisplayName("같은 통화의 환율은 1이어야 한다")
	@Test
	void testIdentityRate() {
		Bank bank = new Bank();
		double rate = bank.rate(USD, USD);
		assertEquals(1, rate);
	}

	@DisplayName("서로 다른 통화 더하기")
	@Test
	void testMixedAddition() {
		Expression fiveBucks = Money.dollar(5);
		Expression tenFrancs = Money.franc(10);
		Bank bank = new Bank();
		bank.addRate(CHF, USD, 2);
		Money result = bank.reduce(fiveBucks.plus(tenFrancs), USD);

		assertEquals(Money.dollar(10), result);
	}

	@DisplayName("Money를 개수로 나눗셈한다")
	@Test
	void testMoneyDivide() {
		Money fiveBucks = Money.dollar(5);
		Bank bank = new Bank();
		bank.addRate(CHF, USD, 2);

		Expression amount = fiveBucks.divide(Count.from(2));
		Money result = bank.reduce(amount, USD);

		assertEquals(Money.dollar(new BigDecimal("2.5")), result);
	}

	@DisplayName("서로 다른 통화를 계속 더하기")
	@Test
	void testSumPlusMoney() {
		Money fiveBucks = Money.dollar(5);
		Money tenFrancs = Money.franc(10);
		Bank bank = new Bank();
		bank.addRate(CHF, USD, 2);
		Expression sum = new Sum(fiveBucks, tenFrancs).plus(fiveBucks);
		Money result = bank.reduce(sum, USD);

		assertEquals(Money.dollar(15), result);
	}

	@DisplayName("합계에서 곱셉을 수행한다")
	@Test
	void testSumTimes() {
		Expression fiveBucks = Money.dollar(5);
		Expression tenFrancs = Money.franc(10);
		Bank bank = new Bank();
		bank.addRate(CHF, USD, 2);

		Expression sum = new Sum(fiveBucks, tenFrancs).times(2);
		Money result = bank.reduce(sum, USD);

		assertEquals(Money.dollar(20), result);
	}

	@DisplayName("뺄셈에서 곱셉을 수행한다")
	@Test
	void testSubtractionTimes() {
		Money tenBucks = Money.dollar(10);
		Expression tenFrancs = Money.franc(10);
		Bank bank = new Bank();
		bank.addRate(CHF, USD, 2);

		Expression subtraction = tenBucks.minus(tenFrancs).times(5);
		Money result = bank.reduce(subtraction, USD);

		assertEquals(Money.dollar(25), result);
	}

	@DisplayName("뺄셈합에서 덧셈을 수행한다")
	@Test
	void testSubtractionPlus() {
		Money fiveBucks = Money.dollar(5);
		Expression tenFrancs = Money.franc(10);
		Bank bank = new Bank();
		bank.addRate(CHF, USD, 2);

		Expression five = fiveBucks.minus(tenFrancs).plus(fiveBucks);
		Money result = bank.reduce(five, USD);

		assertEquals(Money.dollar(5), result);
	}

	@DisplayName("뺄셈합에서 나눗셈을 수행한다")
	@Test
	void testSubtractionDivide() {
		Money fifteen = Money.dollar(15);
		Expression tenFrancs = Money.franc(10);
		Bank bank = new Bank();
		bank.addRate(CHF, USD, 2);

		Expression fiveBucks = fifteen.minus(tenFrancs).divide(Count.from(2));
		Money result = bank.reduce(fiveBucks, USD);

		assertEquals(Money.dollar(5), result);
	}

	@DisplayName("Sum을 개수로 나누어 평균가를 계산한다")
	@Test
	void testSumDivide() {
		Money fiveBucks = Money.dollar(5);
		Money tenFranc = Money.franc(10);
		Bank bank = new Bank();
		bank.addRate(CHF, USD, 2);

		Expression sum = fiveBucks.plus(tenFranc);
		Expression amount = sum.divide(Count.from(2));
		Money result = bank.reduce(amount, USD);

		assertEquals(Money.dollar(5), result);
	}

	@DisplayName("Sum을 Money로 나누어 비율을 계산한다")
	@Test
	void testSumDivideForPercentage() {
		Money fiveBucks = Money.dollar(5);
		Money tenFranc = Money.franc(10);
		Bank bank = new Bank();
		bank.addRate(CHF, USD, 2);

		Expression sum = fiveBucks.plus(tenFranc);
		RateDivision rate = sum.divide(Money.dollar(50));
		Percentage actual = rate.toPercentage(bank, USD);

		assertEquals(Percentage.from(0.2), actual);
	}

	@DisplayName("AverageDivision으로 곱셉을 수행한다")
	@Test
	void testAvgDivisionTimes() {
		Money tenBucks = Money.dollar(10);
		Count divisor = Count.from(2);
		Expression averageDivision = new AverageDivision(tenBucks, divisor);

		Expression result = averageDivision.times(5);
		Money money = result.reduce(Bank.getInstance(), USD);
		assertEquals(Money.dollar(25), money);
	}

	@DisplayName("AverageDivision으로 덧셈을 수행한다")
	@Test
	void testAvgDivisionPlus() {
		Money tenBucks = Money.dollar(10);
		Count divisor = Count.from(2);
		Expression averageDivision = new AverageDivision(tenBucks, divisor);

		Expression result = averageDivision.plus(Money.dollar(5));
		Money money = result.reduce(Bank.getInstance(), USD);
		assertEquals(Money.dollar(10), money);
	}

	@DisplayName("AverageDivision으로 뺄셈을 수행한다")
	@Test
	void testAvgDivisionMinus() {
		Money tenBucks = Money.dollar(10);
		Count divisor = Count.from(2);
		Expression averageDivision = new AverageDivision(tenBucks, divisor);

		Expression result = averageDivision.minus(Money.dollar(5));
		Money money = result.reduce(Bank.getInstance(), USD);
		assertEquals(Money.dollar(0), money);
	}

	@DisplayName("AverageDivision으로 나눗셈하여 평균가를 계산한다")
	@Test
	void testAvgDivisionDivide() {
		Money tenBucks = Money.dollar(10);
		Count divisor = Count.from(2);
		Expression averageDivision = new AverageDivision(tenBucks, divisor);

		Expression result = averageDivision.divide(Count.from(5));
		Money money = result.reduce(Bank.getInstance(), USD);
		assertEquals(Money.dollar(1), money);
	}

	@DisplayName("AverageDivision으로 나눗셈하여 비율을 계산한다")
	@Test
	void testAvgDivisionDivideForRate() {
		Money tenBucks = Money.dollar(10);
		Count divisor = Count.from(2);
		Expression averageDivision = new AverageDivision(tenBucks, divisor);

		RateDivision result = averageDivision.divide(Money.dollar(25));
		Percentage percentage = result.toPercentage(Bank.getInstance(), USD);
		assertEquals(Percentage.from(0.2), percentage);
	}
}
