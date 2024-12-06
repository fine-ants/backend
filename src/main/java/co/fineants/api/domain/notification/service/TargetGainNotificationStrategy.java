package co.fineants.api.domain.notification.service;

import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessageItem;
import co.fineants.api.domain.notification.domain.dto.response.PortfolioNotifyMessageItem;
import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.domain.entity.policy.NotificationPolicy;
import co.fineants.api.domain.notification.domain.entity.policy.TargetGainNotificationPolicy;
import co.fineants.api.domain.notification.repository.NotificationSentRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TargetGainNotificationStrategy implements NotificationStrategy {

	private final TargetGainNotificationPolicy policy;
	private final NotificationSentRepository sentRepository;

	@Override
	public NotificationPolicy<Notifiable> getPolicy() {
		return policy;
	}

	@Override
	public Consumer<Notification> getSendHistory() {
		return sentRepository::addTargetGainSendHistory;
	}

	@Override
	public Function<Notification, NotifyMessageItem> getMapper() {
		return PortfolioNotifyMessageItem::from;
	}
}