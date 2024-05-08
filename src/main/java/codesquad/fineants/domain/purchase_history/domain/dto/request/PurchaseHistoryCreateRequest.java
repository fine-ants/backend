package codesquad.fineants.domain.purchase_history.domain.dto.request;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.count.valiator.CountNumber;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.valiator.MoneyNumber;
import codesquad.fineants.domain.portfolio_holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.purchase_history.domain.entity.PurchaseHistory;
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
	@CountNumber
	private Count numShares;
	@MoneyNumber
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
			.numShares(numShares)
			.purchasePricePerShare(purchasePricePerShare)
			.memo(memo)
			.portfolioHolding(holding)
			.build();
	}
}
