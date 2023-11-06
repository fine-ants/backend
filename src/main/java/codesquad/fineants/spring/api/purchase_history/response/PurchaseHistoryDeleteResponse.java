package codesquad.fineants.spring.api.purchase_history.response;

import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PurchaseHistoryDeleteResponse {
	private Long id;

	public static PurchaseHistoryDeleteResponse from(PurchaseHistory history) {
		return new PurchaseHistoryDeleteResponse(history.getId());
	}
}
