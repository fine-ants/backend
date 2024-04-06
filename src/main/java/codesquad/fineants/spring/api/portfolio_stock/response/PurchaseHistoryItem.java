package codesquad.fineants.spring.api.portfolio_stock.response;

import java.time.LocalDateTime;

import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class PurchaseHistoryItem {
	private Long purchaseHistoryId;
	private LocalDateTime purchaseDate;
	private Count numShares;
	private Money purchasePricePerShare;
	private String memo;

	public static PurchaseHistoryItem from(PurchaseHistory history) {
		return new PurchaseHistoryItem(
			history.getId(),
			history.getPurchaseDate(),
			history.getNumShares(),
			history.getPurchasePricePerShare(),
			history.getMemo());
	}
}
