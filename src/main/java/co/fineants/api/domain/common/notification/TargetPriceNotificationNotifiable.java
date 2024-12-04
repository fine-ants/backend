package co.fineants.api.domain.common.notification;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessage;
import co.fineants.api.domain.notification.domain.entity.type.NotificationType;
import co.fineants.api.domain.notification.repository.NotificationSentRepository;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class TargetPriceNotificationNotifiable implements Notifiable {

	private final String title;
	private final String content;
	private final NotificationType type;
	private final String referenceId;
	private final Long memberId;
	private final String link;
	private final String name;
	private final Money targetPrice;
	private final Long targetPriceNotificationId;
	private final NotificationPreference preference;

	public static TargetPriceNotificationNotifiable from(TargetPriceNotification targetPriceNotification) {
		StockTargetPrice stockTargetPrice = targetPriceNotification.getStockTargetPrice();
		Money targetPrice = targetPriceNotification.getTargetPrice();
		String content = String.format("%s이(가) %s%s에 도달했습니다",
			stockTargetPrice.getStock().getCompanyName(),
			targetPrice.currencySymbol(),
			targetPrice.toDecimalFormat());
		return TargetPriceNotificationNotifiable.builder()
			.title(NotificationType.STOCK_TARGET_PRICE.name())
			.content(content)
			.type(NotificationType.STOCK_TARGET_PRICE)
			.referenceId(stockTargetPrice.getReferenceId())
			.memberId(stockTargetPrice.getMemberId())
			.link(stockTargetPrice.getLink())
			.name(stockTargetPrice.getName())
			.targetPrice(targetPrice)
			.targetPriceNotificationId(targetPriceNotification.getId())
			.build();
	}

	@Override
	public Long fetchMemberId() {
		return memberId;
	}

	@Override
	public NotificationPreference getNotificationPreference() {
		return preference;
	}

	@Override
	public NotifyMessage createMessage(String token) {
		return NotifyMessage.stock(
			title,
			content,
			type,
			referenceId,
			memberId,
			token,
			link,
			name,
			targetPrice,
			targetPriceNotificationId
		);
	}

	@Override
	public NotifyMessage createTargetGainMessageWith(String token) {
		return null;
	}

	@Override
	public NotifyMessage createMaxLossMessageWith(String token) {
		return null;
	}

	@Override
	public NotifyMessage createTargetPriceMessage(String token) {
		return null;
	}

	@Override
	public boolean isReached() {
		return false;
	}

	@Override
	public boolean isActive() {
		return false;
	}

	@Override
	public boolean hasSentHistory(NotificationSentRepository repository) {
		return false;
	}
}
