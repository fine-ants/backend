package codesquad.fineants.domain.notification.domain.entity;

import java.time.LocalDateTime;

import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.notification.domain.entity.type.NotificationType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("P")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioNotification extends Notification {
	private String name;

	private PortfolioNotification(Long id, String title, Boolean isRead, NotificationType type, String referenceId,
		String link, Member member, String name) {
		super(LocalDateTime.now(), null, id, title, isRead, type, referenceId, link, member);
		this.name = name;
	}

	public static PortfolioNotification newNotification(String title, NotificationType type, String referenceId,
		String link, String portfolioName, Member member) {
		return newNotification(null, title, type, referenceId, link, portfolioName, member);
	}

	public static PortfolioNotification newNotification(Long id, String title, NotificationType type,
		String referenceId, String link, String portfolioName, Member member) {
		return new PortfolioNotification(id, title, false, type, referenceId, link, member, portfolioName);
	}

	@Override
	public NotificationBody getBody() {
		return NotificationBody.portfolio(name, getType());
	}

	@Override
	public String getContent() {
		if (getType() == NotificationType.PORTFOLIO_TARGET_GAIN) {
			return String.format("%s의 목표 수익율을 달성했습니다", name);
		} else if (getType() == NotificationType.PORTFOLIO_MAX_LOSS) {
			return String.format("%s이(가) 최대 손실율에 도달했습니다", name);
		}
		throw new IllegalStateException("잘못된 타입입니다. type=" + getType());
	}

	@Override
	public String getName() {
		return name;
	}
}
