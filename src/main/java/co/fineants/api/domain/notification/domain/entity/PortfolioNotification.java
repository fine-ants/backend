package co.fineants.api.domain.notification.domain.entity;

import java.util.List;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessageItem;
import co.fineants.api.domain.notification.domain.dto.response.PortfolioNotifyMessageItem;
import co.fineants.api.domain.notification.domain.entity.type.NotificationType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("P")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioNotification extends Notification {
	private String name;
	private Long portfolioId;

	private PortfolioNotification(Long id, String title, Boolean isRead, NotificationType type, String referenceId,
		String link, Member member, String name, Long portfolioId, List<String> messageIds) {
		super(id, title, isRead, type, referenceId, link, member, messageIds);
		this.name = name;
		this.portfolioId = portfolioId;
	}

	public static PortfolioNotification newNotification(String title, NotificationType type, String referenceId,
		String link, String portfolioName, Long portfolioId, Member member, List<String> messageIds) {
		return newNotification(null, title, type, referenceId, link, portfolioName, portfolioId, member, messageIds);
	}

	public static PortfolioNotification newNotification(Long id, String title, NotificationType type,
		String referenceId, String link, String portfolioName, Long portfolioId, Member member,
		List<String> messageIds) {
		return new PortfolioNotification(id, title, false, type, referenceId, link, member, portfolioName, portfolioId,
			messageIds);
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

	@Override
	public Long getIdToSentHistory() {
		return portfolioId;
	}

	@Override
	public NotifyMessageItem toNotifyMessageItemWith() {
		return PortfolioNotifyMessageItem.create(
			getId(),
			getIsRead(),
			getTitle(),
			getContent(),
			getType(),
			getReferenceId(),
			getMember().getId(),
			getLink(),
			name,
			getMessageIds()
		);
	}
}
