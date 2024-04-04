package codesquad.fineants.spring.api.portfolio_stock.request;

import javax.validation.constraints.NotBlank;

import codesquad.fineants.spring.api.purchase_history.request.PurchaseHistoryCreateRequest;
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
