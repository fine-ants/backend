package codesquad.fineants.domain.purchase_history.domain.dto.response;

import codesquad.fineants.domain.purchase_history.domain.entity.PurchaseHistory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PurchaseHistoryCreateResponse {
	private Long id;
	private Long portfolioId;
	private Long memberId;

	public static PurchaseHistoryCreateResponse from(PurchaseHistory purchaseHistory, Long portfolioId, Long memberId) {
		return new PurchaseHistoryCreateResponse(purchaseHistory.getId(), portfolioId, memberId);
	}
}
