package codesquad.fineants.domain.notification;

import java.time.LocalDateTime;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import codesquad.fineants.domain.member.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("S")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockTargetPriceNotification extends Notification {
	private String stockName;
	private Long targetPrice;

	@Builder
	public StockTargetPriceNotification(LocalDateTime createAt, LocalDateTime modifiedAt, Long id,
		String title, Boolean isRead, String type, String referenceId,
		Member member, String stockName, Long targetPrice) {
		super(createAt, modifiedAt, id, title, isRead, type, referenceId, member);
		this.stockName = stockName;
		this.targetPrice = targetPrice;
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
