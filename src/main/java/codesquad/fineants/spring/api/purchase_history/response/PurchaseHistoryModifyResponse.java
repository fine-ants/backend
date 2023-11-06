package codesquad.fineants.spring.api.purchase_history.response;

import java.time.LocalDateTime;

import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PurchaseHistoryModifyResponse {

	private Long id;
	private LocalDateTime purchaseDate;
	private Double purchasePricePerShare;
	private Long numShares;
	private String memo;

	public static PurchaseHistoryModifyResponse from(PurchaseHistory history) {
		return new PurchaseHistoryModifyResponse(history.getId(), history.getPurchaseDate(),
			history.getPurchasePricePerShare(),
			history.getNumShares(), history.getMemo());
	}
}
