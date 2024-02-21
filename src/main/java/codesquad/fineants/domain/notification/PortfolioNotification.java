package codesquad.fineants.domain.notification;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import codesquad.fineants.domain.member.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("P")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioNotification extends Notification {
	private String portfolioName;
	@Enumerated(value = EnumType.STRING)
	@Column(name = "portfolio_notification_type")
	private PortfolioNotificationType notificationType;

	@Builder
	public PortfolioNotification(LocalDateTime createAt, LocalDateTime modifiedAt, Long id, String title,
		Boolean isRead,
		String type, String referenceId, Member member, String portfolioName,
		PortfolioNotificationType notificationType) {
		super(createAt, modifiedAt, id, title, isRead, type, referenceId, member);
		this.portfolioName = portfolioName;
		this.notificationType = notificationType;
	}

	@Override
	public NotificationBody createNotificationBody() {
		return NotificationBody.builder()
			.name(portfolioName)
			.target(notificationType.getName())
			.build();
	}

	@Override
	public String createNotificationContent() {
		if (notificationType == PortfolioNotificationType.TARGET_GAIN) {
			return String.format("%s의 목표 수익율을 달성했습니다", portfolioName);
		}
		return String.format("%s이(가) 최대 손실율에 도달했습니다", portfolioName);
	}
}
