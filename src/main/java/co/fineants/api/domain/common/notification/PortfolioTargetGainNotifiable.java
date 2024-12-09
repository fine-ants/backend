package co.fineants.api.domain.common.notification;

import co.fineants.api.domain.notification.domain.dto.response.NotifyMessage;
import co.fineants.api.domain.notification.domain.entity.type.NotificationType;
import co.fineants.api.domain.notification.repository.NotificationSentRepository;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.AccessLevel;
import lombok.Builder;

public class PortfolioTargetGainNotifiable implements Notifiable {

	private final String title;
	private final String content;
	private final NotificationType type;
	private final String referenceId;
	private final Long memberId;
	private final String link;
	private final String name;
	private final NotificationPreference preference;
	private final Boolean isReached;
	private final Boolean isActive;
	private final Long id;

	@Builder(access = AccessLevel.PRIVATE)
	private PortfolioTargetGainNotifiable(
		String title,
		String content,
		NotificationType type,
		String referenceId,
		Long memberId,
		String link,
		String name,
		NotificationPreference preference,
		Boolean isReached,
		Boolean isActive,
		Long id) {
		this.title = title;
		this.content = content;
		this.type = type;
		this.referenceId = referenceId;
		this.memberId = memberId;
		this.link = link;
		this.name = name;
		this.preference = preference;
		this.isReached = isReached;
		this.isActive = isActive;
		this.id = id;
	}

	public static PortfolioTargetGainNotifiable from(Portfolio portfolio, Boolean isReached) {
		return PortfolioTargetGainNotifiable.builder()
			.title("포트폴리오")
			.content("%s의 목표 수익률을 달성했습니다".formatted(portfolio.name()))
			.type(NotificationType.PORTFOLIO_TARGET_GAIN)
			.referenceId(portfolio.getReferenceId())
			.memberId(portfolio.getMemberId())
			.link(portfolio.getLink())
			.name(portfolio.name())
			.preference(portfolio.getMember().getNotificationPreference())
			.isReached(isReached)
			.isActive(portfolio.targetGainIsActive())
			.id(portfolio.getId())
			.build();
	}

	@Override
	public Long fetchMemberId() {
		return memberId;
	}

	@Override
	public NotificationPreference getNotificationPreference() {
		return preference;
	}

	@Override
	public NotifyMessage createMessage(String token) {
		return NotifyMessage.portfolio(
			title,
			content,
			type,
			referenceId,
			memberId,
			token,
			link,
			name,
			id
		);
	}

	@Override
	public boolean isReached() {
		return isReached;
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	public boolean emptySentHistory(NotificationSentRepository repository) {
		return !repository.hasTargetGainSendHistory(id);
	}
}
