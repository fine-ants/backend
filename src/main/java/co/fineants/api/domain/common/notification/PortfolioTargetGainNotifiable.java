package co.fineants.api.domain.common.notification;

import co.fineants.api.domain.notification.domain.dto.response.NotifyMessage;
import co.fineants.api.domain.notification.domain.entity.type.NotificationType;
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

	@Builder(access = AccessLevel.PRIVATE)
	private PortfolioTargetGainNotifiable(String title, String content, NotificationType type, String referenceId,
		Long memberId, String link, String name, NotificationPreference preference) {
		this.title = title;
		this.content = content;
		this.type = type;
		this.referenceId = referenceId;
		this.memberId = memberId;
		this.link = link;
		this.name = name;
		this.preference = preference;
	}

	public static PortfolioTargetGainNotifiable from(Portfolio portfolio) {
		return PortfolioTargetGainNotifiable.builder()
			.title("포트폴리오")
			.content(String.format("%s의 목표 수익률을 달성했습니다", portfolio.name()))
			.type(NotificationType.PORTFOLIO_TARGET_GAIN)
			.referenceId(portfolio.getReferenceId())
			.memberId(portfolio.getMemberId())
			.link(portfolio.getLink())
			.name(portfolio.name())
			.preference(portfolio.getMember().getNotificationPreference())
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
			name
		);
	}

	@Override
	public NotifyMessage createTargetGainMessageWith(String token) {
		return null;
	}

	@Override
	public NotifyMessage createMaxLossMessageWith(String token) {
		return null;
	}

	@Override
	public NotifyMessage createTargetPriceMessage(String token) {
		return null;
	}
}
