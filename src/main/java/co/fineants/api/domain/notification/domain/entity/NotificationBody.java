package co.fineants.api.domain.notification.domain.entity;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.notification.domain.entity.type.NotificationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@ToString
public class NotificationBody {
	private String name;
	private String target;

	public static NotificationBody portfolio(String name, NotificationType type) {
		return NotificationBody.builder()
			.name(name)
			.target(type.getName())
			.build();
	}

	public static NotificationBody stock(String tickerSymbol, Money price) {
		return NotificationBody.builder()
			.name(tickerSymbol)
			.target(price.toInteger().toString())
			.build();
	}
}
