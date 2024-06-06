package codesquad.fineants.domain.notification.domain.entity;

import java.time.LocalDateTime;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.MoneyConverter;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.notification.domain.entity.type.NotificationType;
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
		super(LocalDateTime.now(), null, id, title, isRead, type, referenceId, link, member);
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
		String title,
		String referenceId, String link, Long targetPriceNotificationId, Member member) {
		return new StockTargetPriceNotification(id, title, false, NotificationType.STOCK_TARGET_PRICE, referenceId,
			link, member, stockName, targetPrice, targetPriceNotificationId);
	}

	@Override
	public NotificationBody getBody() {
		return NotificationBody.stock(stockName, targetPrice);
	}

	@Override
	public String getContent() {
		return String.format("%s이(가) %s%s에 도달했습니다", stockName, targetPrice.getCurrencySymbol(),
			targetPrice.toDecimalFormat());
	}

	@Override
	public String getName() {
		return stockName;
	}
}
