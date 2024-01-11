package codesquad.fineants.spring.api.portfolio_stock.request;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import org.springframework.data.relational.core.sql.TrueCondition;

import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PortfolioStockCreateRequest {
	@NotBlank(message = "티커심볼은 필수 정보입니다")
	private String tickerSymbol;
	private PurchaseHistoryCreateRequest purchaseHistory;

	@ToString
	@Getter
	public static class PurchaseHistoryCreateRequest {
		private LocalDateTime purchasedDate;
		@Positive(message = "주식 개수는 양수여야 합니다")
		private Long numShares;
		@Positive(message = "매입가는 양수여야 합니다")
		private Double purchasePricePerShare;
		private String memo;
	}

	public boolean isPurchaseHistoryAllNull() {
		return purchaseHistory == null || purchaseHistory.getPurchasedDate() == null && purchaseHistory.getNumShares() == null
			&& purchaseHistory.getPurchasePricePerShare() == null && purchaseHistory.getMemo() == null;
	}

	public boolean isPurchaseHistoryComplete() {
		return purchaseHistory!= null && purchaseHistory.getPurchasedDate() != null && purchaseHistory.getNumShares() != null
			&& purchaseHistory.getPurchasePricePerShare() != null;
	}
}
