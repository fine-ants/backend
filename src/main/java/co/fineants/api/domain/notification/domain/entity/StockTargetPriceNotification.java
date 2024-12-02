package co.fineants.api.domain.notification.domain.entity;

import java.util.List;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.MoneyConverter;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessageItem;
import co.fineants.api.domain.notification.domain.dto.response.TargetPriceNotificationSaveResponse;
import co.fineants.api.domain.notification.domain.dto.response.save.NotificationSaveResponse;
import co.fineants.api.domain.notification.domain.entity.type.NotificationType;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotifyMessageItem;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("S")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class StockTargetPriceNotification extends Notification {
	private String stockName;
	@Convert(converter = MoneyConverter.class)
	private Money targetPrice;
	private Long targetPriceNotificationId;

	private StockTargetPriceNotification(Long id, String title, Boolean isRead, NotificationType type,
		String referenceId, String link, Member member,
		String stockName, Money targetPrice, Long targetPriceNotificationId) {
		super(id, title, isRead, type, referenceId, link, member);
		this.stockName = stockName;
		this.targetPrice = targetPrice;
		this.targetPriceNotificationId = targetPriceNotificationId;
	}

	public static StockTargetPriceNotification newNotification(String stockName, Money targetPrice, String title,
		String referenceId, String link, Long targetPriceNotificationId, Member member) {
		return newNotification(null, stockName, targetPrice, title, referenceId, link, targetPriceNotificationId,
			member);
	}

	public static StockTargetPriceNotification newNotification(Long id, String stockName, Money targetPrice,
		String title, String referenceId, String link, Long targetPriceNotificationId, Member member) {
		return new StockTargetPriceNotification(id, title, false, NotificationType.STOCK_TARGET_PRICE, referenceId,
			link, member, stockName, targetPrice, targetPriceNotificationId);
	}

	@Override
	public NotificationBody getBody() {
		return NotificationBody.stock(stockName, targetPrice);
	}

	@Override
	public String getContent() {
		return String.format("%s이(가) %s%s에 도달했습니다", stockName, targetPrice.currencySymbol(),
			targetPrice.toDecimalFormat());
	}

	@Override
	public String getName() {
		return stockName;
	}

	@Override
	public NotificationSaveResponse toSaveResponse() {
		return TargetPriceNotificationSaveResponse.from(this);
	}

	@Override
	public String getIdToSentHistory() {
		return String.format("targetPriceNotification:%d", targetPriceNotificationId);
	}

	@Override
	public NotifyMessageItem toNotifyMessageItemWith(List<String> messageIds) {
		return TargetPriceNotifyMessageItem.builder()
			.notificationId(getId())
			.isRead(getIsRead())
			.title(getTitle())
			.content(getContent())
			.type(getType())
			.referenceId(getReferenceId())
			.memberId(getMember().getId())
			.link(getLink())
			.messageIds(messageIds)
			.stockName(stockName)
			.targetPrice(targetPrice)
			.targetPriceNotificationId(targetPriceNotificationId)
			.build();
	}
}
