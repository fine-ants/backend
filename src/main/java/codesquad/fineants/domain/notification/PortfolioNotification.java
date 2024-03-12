package codesquad.fineants.domain.notification;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.notification.type.NotificationType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("P")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioNotification extends Notification {
	private String portfolioName;

	@Builder
	public PortfolioNotification(Long id, String title, Boolean isRead, NotificationType type, String referenceId,
		String messageId, Member member, String portfolioName) {
		super(id, title, isRead, type, referenceId, messageId, member);
		this.portfolioName = portfolioName;
	}

	@Override
	public NotificationBody createNotificationBody() {
		return NotificationBody.builder()
			.name(portfolioName)
			.target(getType().getName())
			.build();
	}

	@Override
	public String createNotificationContent() {
		if (getType() == NotificationType.PORTFOLIO_TARGET_GAIN) {
			return String.format("%s의 목표 수익율을 달성했습니다", portfolioName);
		} else if (getType() == NotificationType.PORTFOLIO_MAX_LOSS) {
			return String.format("%s이(가) 최대 손실율에 도달했습니다", portfolioName);
		}
		throw new IllegalStateException("잘못된 타입입니다. type=" + getType());
	}
}
