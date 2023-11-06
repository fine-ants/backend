package codesquad.fineants.spring.api.purchase_history.request;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PurchaseHistoryCreateRequest {
	@NotNull(message = "매입날짜는 날짜 형식의 필수 정보입니다")
	private LocalDateTime purchaseDate;
	@NotNull(message = "매입 개수 정보는 필수 정보입니다")
	@Positive(message = "주식 개수는 양수여야 합니다")
	private Long numShares;
	@NotNull(message = "매입가는 필수 정보입니다")
	@Positive(message = "매입가는 양수여야 합니다")
	private Double purchasePricePerShare;
	private String memo;

	public PurchaseHistory toEntity(PortfolioHolding portfolioHolding) {
		return PurchaseHistory.builder()
			.purchaseDate(purchaseDate)
			.numShares(numShares)
			.purchasePricePerShare(purchasePricePerShare)
			.memo(memo)
			.portFolioHolding(portfolioHolding)
			.build();
	}
}
