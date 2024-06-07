package codesquad.fineants.domain.stock_target_price.domain.dto.request;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.valiator.MoneyNumber;
import codesquad.fineants.domain.stock_target_price.domain.entity.StockTargetPrice;
import codesquad.fineants.domain.stock_target_price.domain.entity.TargetPriceNotification;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TargetPriceNotificationCreateRequest {

	@NotNull(message = "필수 정보입니다")
	private String tickerSymbol;

	@MoneyNumber
	private Money targetPrice;

	public TargetPriceNotification toEntity(StockTargetPrice stockTargetPrice) {
		return TargetPriceNotification.newTargetPriceNotification(targetPrice, stockTargetPrice);
	}
}
