package codesquad.fineants.spring.api.purchase_history.request;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.count.annotation.StockCount;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.annotation.PurchasePrice;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
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
public class PurchaseHistoryCreateRequest {
	@NotNull(message = "매입날짜는 날짜 형식의 필수 정보입니다")
	private LocalDateTime purchaseDate;
	@StockCount
	private Count numShares;
	@PurchasePrice
	private Money purchasePricePerShare;
	private String memo;

	public static PurchaseHistoryCreateRequest create(LocalDateTime purchaseDate, Count numShares,
		Money purchasePricePerShare, String memo) {
		return PurchaseHistoryCreateRequest.builder()
			.purchaseDate(purchaseDate)
			.numShares(numShares)
			.purchasePricePerShare(purchasePricePerShare)
			.memo(memo)
			.build();
	}

	public PurchaseHistory toEntity(PortfolioHolding holding) {
		return PurchaseHistory.builder()
			.purchaseDate(purchaseDate)
			.numShares(numShares.getValue().longValue())
			.purchasePricePerShare(purchasePricePerShare.getAmount().doubleValue())
			.memo(memo)
			.portfolioHolding(holding)
			.build();
	}
}
