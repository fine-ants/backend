package co.fineants.api.domain.portfolio.domain.entity;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.api.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.RateDivision;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioPieChartItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioSectorChartItem;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.stock.domain.entity.Stock;

class PortfolioTest extends AbstractContainerBaseTest {

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
		Expression result = portfolio.calculateTotalGain();

		// then
		Money amount = Bank.getInstance().toWon(result);
		assertThat(amount).isEqualByComparingTo(Money.won(100000L));
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
		RateDivision result = portfolio.calculateTotalGainRate();

		// then
		Money totalGainAmount = Money.won(100000);
		Money totalInvestmentAmount = Money.won(100000);
		RateDivision expected = totalGainAmount.divide(totalInvestmentAmount);
		assertThat(result).isEqualByComparingTo(expected);
	}

	@DisplayName("사용자는 포트폴리오의 파이 차트를 요청한다")
	@Test
	void createPieChart() {
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
		List<PortfolioPieChartItem> items = portfolio.createPieChart();

		// then
		assertThat(items)
			.asList()
			.hasSize(3)
			.extracting("name", "valuation", "totalGain")
			.usingComparatorForType(Money::compareTo, Money.class)
			.containsExactlyInAnyOrder(
				Tuple.tuple("현금", Money.won(850000), Money.zero()),
				Tuple.tuple("삼성전자보통주", Money.won(100000), Money.won(50000)),
				Tuple.tuple("동화약품보통주", Money.won(100000), Money.zero()));
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
		List<PortfolioSectorChartItem> items = portfolio.createSectorChart();

		// then
		assertThat(items)
			.asList()
			.hasSize(3)
			.extracting("sector")
			.containsExactlyInAnyOrder("현금", "의약품", "전기전자");
	}
}
