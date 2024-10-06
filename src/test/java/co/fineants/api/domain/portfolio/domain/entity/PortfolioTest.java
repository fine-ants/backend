package co.fineants.api.domain.portfolio.domain.entity;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioPieChartItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioSectorChartItem;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
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
		PortfolioCalculator calculator = new PortfolioCalculator();
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

		PortfolioCalculator calculator = new PortfolioCalculator();
		// when
		Expression result = calculator.calTotalGainRateBy(portfolio);

		// then
		Money totalGainAmount = Money.won(100000);
		Money totalInvestmentAmount = Money.won(100000);
		Expression expected = totalGainAmount.divide(totalInvestmentAmount);
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

		PortfolioCalculator calculator = new PortfolioCalculator();
		Expression balance = calculator.calBalanceBy(portfolio);
		Expression totalAsset = calculator.calTotalAssetBy(portfolio);
		// when
		List<PortfolioPieChartItem> items = portfolio.createPieChart(balance, totalAsset);

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

		PortfolioCalculator calculator = new PortfolioCalculator();
		Expression balance = calculator.calBalanceBy(portfolio);
		Expression totalCurrentValuation = calculator.calTotalCurrentValuationBy(portfolio);
		Expression totalAsset = calculator.calTotalAsset(balance, totalCurrentValuation);
		// when
		List<PortfolioSectorChartItem> items = portfolio.createSectorChart(balance, totalAsset);

		// then
		assertThat(items)
			.asList()
			.hasSize(3)
			.extracting("sector")
			.containsExactlyInAnyOrder("현금", "의약품", "전기전자");
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
