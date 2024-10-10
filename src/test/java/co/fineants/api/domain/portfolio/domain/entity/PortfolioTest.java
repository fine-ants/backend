package co.fineants.api.domain.portfolio.domain.entity;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.stock.domain.entity.Stock;

class PortfolioTest extends AbstractContainerBaseTest {

	@Autowired
	private PortfolioCalculator calculator;

	@DisplayName("포트폴리오의 총 손익을 계산한다")
	@Test
	void calculateTotalGain() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding portFolioHolding = PortfolioHolding.of(portfolio, stock, Money.won(20000L));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(5);
		Money purchasePerShare = Money.won(10000);
		String memo = "첫구매";
		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo,
			portFolioHolding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo,
			portFolioHolding);

		portFolioHolding.addPurchaseHistory(purchaseHistory1);
		portFolioHolding.addPurchaseHistory(purchaseHistory2);

		portfolio.addHolding(portFolioHolding);
		// when
		Expression result = calculator.calTotalGainBy(portfolio);

		// then
		assertThat(result).isEqualByComparingTo(Money.won(100000L));
	}

	@DisplayName("포트폴리오의 총 손익율 계산한다")
	@Test
	void calculateTotalReturnRate() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding portFolioHolding = PortfolioHolding.of(portfolio, stock, Money.won(20000L));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(5);
		Money purchasePerShare = Money.won(10000);
		String memo = "첫구매";
		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo,
			portFolioHolding);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo,
			portFolioHolding);

		portFolioHolding.addPurchaseHistory(purchaseHistory1);
		portFolioHolding.addPurchaseHistory(purchaseHistory2);

		portfolio.addHolding(portFolioHolding);

		// when
		Expression result = calculator.calTotalGainRateBy(portfolio);

		// then
		Money totalGainAmount = Money.won(100000);
		Money totalInvestmentAmount = Money.won(100000);
		Expression expected = totalGainAmount.divide(totalInvestmentAmount);
		assertThat(result).isEqualByComparingTo(expected);
	}

	@DisplayName("사용자는 포트폴리오의 섹터 차트를 요청한다")
	@Test
	void createSectorChart() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		Stock stock2 = createDongwhaPharmStock();
		PortfolioHolding holding1 = PortfolioHolding.of(portfolio, stock, Money.won(20000L));
		PortfolioHolding holding2 = PortfolioHolding.of(portfolio, stock2, Money.won(20000L));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(5);
		Money purchasePerShare = Money.won(10000);
		String memo = "첫구매";
		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo,
			holding1);

		numShares = Count.from(5);
		purchasePerShare = Money.won(20000);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo,
			holding2);

		holding1.addPurchaseHistory(purchaseHistory1);
		holding2.addPurchaseHistory(purchaseHistory2);

		portfolio.addHolding(holding1);
		portfolio.addHolding(holding2);

		// when
		Map<String, List<Expression>> result = calculator.calSectorChartBy(portfolio);

		// then
		Map<String, List<Expression>> expected = Map.of(
			"현금", List.of(Money.won(850_000)),
			"의약품", List.of(Money.won(100_000)),
			"전기전자", List.of(Money.won(100_000))
		);
		Bank bank = Bank.getInstance();
		Map<String, List<Money>> actual = result.entrySet().stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> entry.getValue().stream()
					.map(bank::toWon)
					.toList())
			);
		assertThat(actual)
			.usingComparatorForType(Expression::compareTo, Expression.class)
			.isEqualTo(expected);
	}

	@DisplayName("포트폴리오에 포트폴리오 종목을 추가한다")
	@Test
	void addHoldings() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		PortfolioHolding holding1 = PortfolioHolding.of(portfolio, stock, Money.won(20000L));
		// when
		portfolio.addHolding(holding1);
		// then
		assertThat(portfolio.getPortfolioHoldings())
			.hasSize(1)
			.containsExactlyInAnyOrder(PortfolioHolding.of(portfolio, stock, Money.won(20000L)));
		assertThat(holding1.getPortfolio()).isEqualTo(portfolio);
	}

	@DisplayName("포트폴리오 인스턴스 생성시 회원 객체 전달하면 회원도 같이 설정된다")
	@Test
	void setMember_givenMember_whenCreatingInstance_thenSetMember() {
		// given
		Member member = createMember();
		// when
		Portfolio portfolio = createPortfolio(member);
		// then
		Assertions.assertThat(portfolio.getMember()).isEqualTo(createMember());
	}
}
