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
		NotificationType type, String referenceId, String messageId,
		Member member, String stockName, Long targetPrice,
		Long targetPriceNotificationId) {
		super(id, title, isRead, type, referenceId, messageId, member);
		this.stockName = stockName;
		this.targetPrice = targetPrice;
		this.targetPriceNotificationId = targetPriceNotificationId;
	}

	@Override
	public NotificationBody createNotificationBody() {
		return NotificationBody.builder()
			.name(stockName)
			.target(targetPrice.toString())
			.build();
	}

	@Override
	public String createNotificationContent() {
		return String.format("%s(이)가 지정가 KRW%s에 도달했습니다", stockName, targetPrice);
	}

}
