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

public class TargetPriceNotificationNotifiable implements Notifiable {

	private final String title;
	private final String content;
	private final NotificationType type;
	private final String referenceId;
	private final Long memberId;
	private final String link;
	private final String name;
	private final Money targetPrice;
	private final NotificationPreference preference;
	private final Boolean isReached;
	private final Boolean isActive;
	private final Long id;

	@Builder(access = AccessLevel.PRIVATE)
	private TargetPriceNotificationNotifiable(
		String title,
		String content,
		NotificationType type,
		String referenceId,
		Long memberId,
		String link,
		String name,
		Money targetPrice,
		NotificationPreference preference,
		Boolean isReached,
		Boolean isActive,
		Long id) {
		this.title = title;
		this.content = content;
		this.type = type;
		this.referenceId = referenceId;
		this.memberId = memberId;
		this.link = link;
		this.name = name;
		this.targetPrice = targetPrice;
		this.preference = preference;
		this.isReached = isReached;
		this.isActive = isActive;
		this.id = id;
	}

	public static TargetPriceNotificationNotifiable from(
		TargetPriceNotification targetPriceNotification,
		Boolean isReached) {
		StockTargetPrice stockTargetPrice = targetPriceNotification.getStockTargetPrice();
		Money targetPrice = targetPriceNotification.getTargetPrice();
		String content = String.format("%s이(가) %s%s에 도달했습니다",
			stockTargetPrice.getStock().getCompanyName(),
			targetPrice.currencySymbol(),
			targetPrice.toDecimalFormat());
		return TargetPriceNotificationNotifiable.builder()
			.title(NotificationType.STOCK_TARGET_PRICE.getName())
			.content(content)
			.type(NotificationType.STOCK_TARGET_PRICE)
			.referenceId(stockTargetPrice.getReferenceId())
			.memberId(stockTargetPrice.getMemberId())
			.link(stockTargetPrice.getLink())
			.name(stockTargetPrice.getName())
			.targetPrice(targetPrice)
			.preference(stockTargetPrice.getMember().getNotificationPreference())
			.isReached(isReached)
			.isActive(targetPriceNotification.isActive())
			.id(targetPriceNotification.getId())
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
			id
		);
	}
	
	@Override
	public boolean isReached() {
		return isReached;
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	public boolean hasSentHistory(NotificationSentRepository repository) {
		return repository.hasTargetPriceSendHistory(id);
	}
}
