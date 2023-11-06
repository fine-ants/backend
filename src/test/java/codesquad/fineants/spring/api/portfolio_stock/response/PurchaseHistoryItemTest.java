package codesquad.fineants.spring.api.portfolio_stock.response;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.domain.purchase_history.PurchaseHistory;

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
			.numShares(3L)
			.purchasePricePerShare(30000.0)
			.memo("첫구매")
			.build();
		// when
		PurchaseHistoryItem historyItem = PurchaseHistoryItem.from(history);
		// then
		assertThat(historyItem)
			.extracting("purchaseHistoryId", "purchaseDate", "numShares", "purchasePricePerShare", "memo")
			.containsExactlyInAnyOrder(1L, now, 3L, 30000.0, "첫구매");
	}
}
