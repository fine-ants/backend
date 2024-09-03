package co.fineants.api.domain.holding.domain.dto.response;

import java.time.LocalDateTime;

import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
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
