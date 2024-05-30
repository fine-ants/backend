package codesquad.fineants.domain.notification.domain.entity;

import java.time.LocalDateTime;

import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.notification.domain.entity.type.NotificationType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("P")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioNotification extends Notification {
	private String name;

	@Builder
	public PortfolioNotification(LocalDateTime createAt, LocalDateTime modifiedAt, Long id,
		String title, Boolean isRead, NotificationType type, String referenceId, String link, Member member,
		String name) {
		super(createAt, modifiedAt, id, title, isRead, type, referenceId, link, member);
		this.name = name;
	}

	public static Notification create(String portfolioName, String title, NotificationType type, String referenceId,
		String link, Member member) {
		return PortfolioNotification.builder()
			.name(portfolioName)
			.title(title)
			.isRead(false)
			.type(type)
			.referenceId(referenceId)
			.link(link)
			.member(member)
			.build();
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
