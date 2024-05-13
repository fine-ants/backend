package codesquad.fineants.domain.portfolio_holding.domain.dto.request;

import codesquad.fineants.domain.purchase_history.domain.dto.request.PurchaseHistoryCreateRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PortfolioHoldingCreateRequest {
	@NotBlank(message = "티커심볼은 필수 정보입니다")
	private String tickerSymbol;
	private PurchaseHistoryCreateRequest purchaseHistory;

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
