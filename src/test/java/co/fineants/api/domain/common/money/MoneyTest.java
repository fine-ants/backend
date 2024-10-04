package co.fineants.api.domain.common.money;

import static co.fineants.api.domain.common.money.Currency.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.api.domain.common.count.Count;

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
	}

	@DisplayName("머니 equals")
	@Test
	void testEquals() {
		Money fiveBucks = Money.dollar(5);

		boolean actual = fiveBucks.equals(fiveBucks);

		assertTrue(actual);
	}

	@DisplayName("머니 퍼센티지로 변환")
	@Test
	void testToPercentage() {
		Expression fiveBucks = Money.dollar(5);

		Percentage percentage = fiveBucks.toPercentage(Bank.getInstance(), USD);

		assertEquals(Percentage.from(5.0), percentage);
	}

	@DisplayName("Sum과 Money의 동치성 테스트")
	@Test
	void testEqualityForOtherType() {
		Money won = Money.won(1000);
		Sum sum = new Sum(Money.won(1000), Money.wonZero());
		assertNotEquals(won, sum);
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

	@DisplayName("AverageDivision을 대소 비교한다")
	@Test
	void testAvgDivisionCompareTo() {
		Money tenBucks = Money.dollar(10);
		Count count = Count.from(2);
		Expression avgDivision = tenBucks.divide(count);
		Bank.getInstance().addRate(USD, KRW, 0.001);

		int actual = avgDivision.compareTo(Money.dollar(5));
		int actual2 = avgDivision.compareTo(Money.dollar(new BigDecimal("5.0")));
		int actual3 = avgDivision.compareTo(Money.dollar(6));
		int actual4 = avgDivision.compareTo(Money.dollar(4));

		assertEquals(0, actual);
		assertEquals(0, actual2);
		assertEquals(-1, actual3);
		assertEquals(1, actual4);
	}

	@DisplayName("평균을 비율로 변환한다")
	@Test
	void testAvgDivisionToPercentage() {
		Money tenBucks = Money.dollar(10);
		Count count = Count.from(2);
		Expression avgDivision = tenBucks.divide(count);

		Percentage percentage = avgDivision.toPercentage(Bank.getInstance(), USD);

		assertEquals(Percentage.from(5.0), percentage);
	}

	@DisplayName("비율의 대소여부를 비교한다")
	@Test
	void testRateDivisionCompareTo() {
		Money tenBucks = Money.dollar(10);
		Money fiveBucks = Money.dollar(5);
		Bank.getInstance().addRate(USD, KRW, 0.001);

		Expression twoRate = tenBucks.divide(fiveBucks);
		RateDivision fiveRate = tenBucks.divide(Money.dollar(2));
		int actual = twoRate.compareTo(fiveRate);

		RateDivision twoRate2 = tenBucks.divide(fiveBucks);
		int actual2 = twoRate.compareTo(twoRate2);

		RateDivision oneRate = tenBucks.divide(Money.dollar(10));
		int actual3 = twoRate.compareTo(oneRate);

		assertEquals(-1, actual);
		assertEquals(0, actual2);
		assertEquals(1, actual3);
	}

	@DisplayName("비율을 곱셈한다")
	@Test
	void testRateDivisionTimes() {
		Money tenBucks = Money.dollar(10);
		Money fiveBucks = Money.dollar(5);

		RateDivision twoRate = tenBucks.divide(fiveBucks);
		RateDivision tenRate = (RateDivision)twoRate.times(5);

		Percentage expected = Percentage.from(10);
		Percentage actual = tenRate.toPercentage(Bank.getInstance(), USD);
		assertEquals(expected, actual);
	}

	@DisplayName("비율을 덧셈한다")
	@Test
	void testRateDivisionPlus() {
		Money tenBucks = Money.dollar(10);
		Money fiveBucks = Money.dollar(5);

		RateDivision rateDivision = tenBucks.divide(fiveBucks);
		RateDivision addend = new RateDivision(Money.dollar(10), Money.dollar(2));
		Expression sum = rateDivision.plus(addend);

		Bank bank = Bank.getInstance();
		Percentage expected = Percentage.from(7.0);
		Percentage actual = sum.toPercentage(bank, USD);
		assertEquals(expected, actual);
	}

	@DisplayName("RateDivision을 제외한 덧셈 연산은 지원하지 않는다")
	@Test
	void testRateDivisionPlus_NotSupportedRateDivisionOther() {
		Money tenBucks = Money.dollar(10);
		Money fiveBucks = Money.dollar(5);
		RateDivision rateDivision = tenBucks.divide(fiveBucks);

		Throwable throwable = Assertions.catchThrowable(() -> rateDivision.plus(Money.dollar(5)));
		Assertions.assertThat(throwable)
			.isInstanceOf(IllegalArgumentException.class);
	}

	@DisplayName("비율을 뺄셈한다")
	@Test
	void testRateDivisionMinus() {
		Money tenBucks = Money.dollar(10);
		Money fiveBucks = Money.dollar(5);

		RateDivision rateDivision = tenBucks.divide(fiveBucks);
		RateDivision addend = new RateDivision(Money.dollar(10), Money.dollar(2));
		Expression minus = rateDivision.minus(addend);

		Bank bank = Bank.getInstance();
		Percentage expected = new RateDivision(Money.dollar(-30), Money.dollar(10)).toPercentage(bank, USD);
		Percentage actual = minus.toPercentage(bank, USD);
		assertEquals(expected, actual);
	}

	@DisplayName("비율을 나눗셈한다")
	@Test
	void testRateDivisionDivide() {
		Money tenBucks = Money.dollar(10);
		Money fiveBucks = Money.dollar(5);
		RateDivision twoRate = tenBucks.divide(fiveBucks);
		Money twoBucks = Money.dollar(2);
		RateDivision fiveRate = tenBucks.divide(twoBucks);

		RateDivision result = twoRate.divide(fiveRate);

		Percentage expected = Percentage.from(0.4);
		Percentage actual = result.toPercentage(Bank.getInstance(), USD);
		assertEquals(expected, actual);
	}

	@DisplayName("음수를 가진 Money를 생성할 수 없다")
	@Test
	void testConstructor_givenNegativeInfo_whenCreatingInstance_thenThrowException() {
		// given
		int amount = -1000;
		// when
		Throwable throwable = Assertions.catchThrowable(() -> Money.won(amount));
		// then
		Assertions.assertThat(throwable)
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Money amount must be greater than zero");

	}
}
