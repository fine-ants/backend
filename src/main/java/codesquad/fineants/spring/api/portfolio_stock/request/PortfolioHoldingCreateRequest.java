package codesquad.fineants.spring.api.portfolio_stock.request;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
public class PortfolioHoldingCreateRequest {
	@NotBlank(message = "티커심볼은 필수 정보입니다")
	private String tickerSymbol;
	private PurchaseHistoryCreateRequest purchaseHistory;

	@ToString
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@Getter
	public static class PurchaseHistoryCreateRequest {
		private LocalDateTime purchaseDate;
		@Positive(message = "주식 개수는 양수여야 합니다")
		private Long numShares;
		@Positive(message = "매입가는 양수여야 합니다")
		private Double purchasePricePerShare;
		private String memo;

		public static PurchaseHistoryCreateRequest create(LocalDateTime purchaseDate, Long numShares,
			Double purchasePricePerShare, String memo) {
			return PurchaseHistoryCreateRequest.builder()
				.purchaseDate(purchaseDate)
				.numShares(numShares)
				.purchasePricePerShare(purchasePricePerShare)
				.memo(memo)
				.build();
		}
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
