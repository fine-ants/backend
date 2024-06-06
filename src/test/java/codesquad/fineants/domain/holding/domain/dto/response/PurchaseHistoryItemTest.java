package codesquad.fineants.domain.holding.domain.dto.response;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.purchasehistory.domain.entity.PurchaseHistory;
import codesquad.fineants.domain.stock.domain.entity.Stock;

class PurchaseHistoryItemTest extends AbstractContainerBaseTest {

	@DisplayName("엔티티 객체를 dto 객체로 변환한다")
	@Test
	void from() {
		// given
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);
		Stock samsungStock = createSamsungStock();
		PortfolioHolding holding = createPortfolioHolding(portfolio, samsungStock);

		LocalDateTime now = LocalDateTime.now();
		Count numShares = Count.from(3);
		Money purchasePerShare = Money.won(30000);
		String memo = "첫구매";
		PurchaseHistory history = createPurchaseHistory(1L, now, numShares, purchasePerShare, memo,
			holding);

		// when
		PurchaseHistoryItem historyItem = PurchaseHistoryItem.from(history);
		// then
		assertThat(historyItem)
			.extracting(
				PurchaseHistoryItem::getPurchaseHistoryId,
				PurchaseHistoryItem::getPurchaseDate,
				PurchaseHistoryItem::getNumShares,
				PurchaseHistoryItem::getPurchasePricePerShare,
				PurchaseHistoryItem::getMemo
			)
			.usingComparatorForType(Money::compareTo, Money.class)
			.usingComparatorForType(Count::compareTo, Count.class)
			.containsExactlyInAnyOrder(
				1L,
				now,
				Count.from(3L),
				Money.won(30000L),
				"첫구매"
			);
	}
}
