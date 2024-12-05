package co.fineants.api.domain.notification.domain.entity;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.MoneyConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("S")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@SuperBuilder(toBuilder = true)
public class StockTargetPriceNotification extends Notification {
	private String stockName;
	@Convert(converter = MoneyConverter.class)
	private Money targetPrice;
	private Long targetPriceNotificationId;

	@Override
	public Notification withId(Long id) {
		return this.toBuilder()
			.id(id)
			.build();
	}

	@Override
	public NotificationBody getBody() {
		return NotificationBody.stock(stockName, targetPrice);
	}

	@Override
	public String getContent() {
		return "%s이(가) %s%s에 도달했습니다".formatted(stockName, targetPrice.currencySymbol(), targetPrice.toDecimalFormat());
	}

	@Override
	public String getName() {
		return stockName;
	}

	@Override
	public Long getIdToSentHistory() {
		return targetPriceNotificationId;
	}
}
