package codesquad.fineants.spring.api.stock_target_price.request;

import javax.validation.constraints.NotNull;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.valiator.MoneyNumber;
import codesquad.fineants.domain.stock_target_price.StockTargetPrice;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotification;
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
		return TargetPriceNotification.builder()
			.targetPrice(targetPrice)
			.stockTargetPrice(stockTargetPrice)
			.build();
	}
}
