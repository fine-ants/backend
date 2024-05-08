package codesquad.fineants.domain.portfolio_holding.domain.dto.response;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.purchase_history.domain.entity.PurchaseHistory;

@ActiveProfiles("test")
class PurchaseHistoryItemTest {

	@DisplayName("엔티티 객체를 dto 객체로 변환한다")
	@Test
	void from() {
		// given
		LocalDateTime now = LocalDateTime.now();
		PurchaseHistory history = PurchaseHistory.builder()
			.id(1L)
			.purchaseDate(now)
			.numShares(Count.from(3L))
			.purchasePricePerShare(Money.won(30000.0))
			.memo("첫구매")
			.build();
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
