package codesquad.fineants.spring.api.portfolio_stock.response;

import java.time.LocalDateTime;

import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PurchaseHistoryItem {
	private Long purchaseHistoryId;
	private LocalDateTime purchaseDate;
	private Long numShares;
	private Double purchasePricePerShare;
	private String memo;

	public static PurchaseHistoryItem from(PurchaseHistory history) {
		return new PurchaseHistoryItem(history.getId(), history.getPurchaseDate(), history.getNumShares(),
			history.getPurchasePricePerShare(),
			history.getMemo());
	}
}
