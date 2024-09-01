package co.fineants.api.domain.purchasehistory.domain.dto.response;

import java.time.LocalDateTime;

import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class PurchaseHistoryUpdateResponse {

	private Long id;
	private LocalDateTime purchaseDate;
	private Money purchasePricePerShare;
	private Count numShares;
	private String memo;
	private Long portfolioId;
	private Long memberId;

	public static PurchaseHistoryUpdateResponse from(PurchaseHistory history, Long portfolioId, Long memberId) {
		return PurchaseHistoryUpdateResponse.builder()
			.id(history.getId())
			.purchaseDate(history.getPurchaseDate())
			.purchasePricePerShare(history.getPurchasePricePerShare())
			.numShares(history.getNumShares())
			.memo(history.getMemo())
			.portfolioId(portfolioId)
			.memberId(memberId)
			.build();
	}
}
