package codesquad.fineants.spring.api.purchase_history.response;

import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PurchaseHistoryCreateResponse {
	private Long id;

	public static PurchaseHistoryCreateResponse from(PurchaseHistory purchaseHistory) {
		return new PurchaseHistoryCreateResponse(purchaseHistory.getId());
	}
}
