package codesquad.fineants.domain.notification;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.notification.type.NotificationType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("S")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class StockTargetPriceNotification extends Notification {
	private String stockName;
	private Long targetPrice;
	private Long targetPriceNotificationId;

	@Builder
	public StockTargetPriceNotification(Long id, String title, Boolean isRead,
		NotificationType type, String referenceId, String link,
		Member member, String stockName, Long targetPrice,
		Long targetPriceNotificationId) {
		super(id, title, isRead, type, referenceId, link, member);
		this.stockName = stockName;
		this.targetPrice = targetPrice;
		this.targetPriceNotificationId = targetPriceNotificationId;
	}

	public static StockTargetPriceNotification create(String stockName, Long targetPrice, String title,
		String referenceId, String link, Long targetPriceNotificationId, Member member) {
		return StockTargetPriceNotification.builder()
			.stockName(stockName)
			.targetPrice(targetPrice)
			.title(title)
			.isRead(false)
			.type(NotificationType.STOCK_TARGET_PRICE)
			.referenceId(referenceId)
			.link(link)
			.member(member)
			.targetPriceNotificationId(targetPriceNotificationId)
			.build();
	}

	@Override
	public NotificationBody getBody() {
		return NotificationBody.stock(stockName, targetPrice);
	}

	@Override
	public String getContent() {
		return String.format("%s(이)가 지정가 KRW%s에 도달했습니다", stockName, targetPrice);
	}

	@Override
	public String getName() {
		return stockName;
	}
}
