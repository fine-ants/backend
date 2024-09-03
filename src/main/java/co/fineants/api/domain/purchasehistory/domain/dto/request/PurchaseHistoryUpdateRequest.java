package co.fineants.api.domain.purchasehistory.domain.dto.request;

import java.time.LocalDateTime;

import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.count.valiator.CountNumber;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.valiator.MoneyNumber;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import jakarta.validation.constraints.NotNull;
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
public class PurchaseHistoryUpdateRequest {
	@NotNull(message = "매입날짜는 날짜 형식의 필수 정보입니다")
	private LocalDateTime purchaseDate;
	@CountNumber
	private Count numShares;
	@MoneyNumber
	private Money purchasePricePerShare;
	private String memo;

	public PurchaseHistory toEntity(PortfolioHolding portfolioHolding) {
		return PurchaseHistory.create(purchaseDate, numShares, purchasePricePerShare, memo, portfolioHolding);
	}
}
