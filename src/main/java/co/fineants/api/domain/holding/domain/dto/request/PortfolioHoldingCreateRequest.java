package co.fineants.api.domain.holding.domain.dto.request;

import co.fineants.api.domain.purchasehistory.domain.dto.request.PurchaseHistoryCreateRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class PortfolioHoldingCreateRequest {
	@NotBlank(message = "티커심볼은 필수 정보입니다")
	private String tickerSymbol;
	private PurchaseHistoryCreateRequest purchaseHistory;

	public static PortfolioHoldingCreateRequest create(String tickerSymbol,
		PurchaseHistoryCreateRequest purchaseHistory) {
		return new PortfolioHoldingCreateRequest(tickerSymbol, purchaseHistory);
	}

	public boolean isPurchaseHistoryAllNull() {
		return purchaseHistory == null
			|| purchaseHistory.getPurchaseDate() == null && purchaseHistory.getNumShares() == null
			&& purchaseHistory.getPurchasePricePerShare() == null && purchaseHistory.getMemo() == null;
	}

	public boolean isPurchaseHistoryComplete() {
		return purchaseHistory != null && purchaseHistory.getPurchaseDate() != null
			&& purchaseHistory.getNumShares() != null
			&& purchaseHistory.getPurchasePricePerShare() != null;
	}
}
