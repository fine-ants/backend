package co.fineants.api.domain.notification.service.disptacher;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.notification.domain.dto.response.SentNotifyMessage;
import co.fineants.api.domain.notification.domain.entity.policy.NotificationPolicy;
import co.fineants.api.domain.notification.service.provider.NotificationProvider;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationDispatcher {
	private final List<NotificationProvider<Notifiable>> providers;

	public List<SentNotifyMessage> dispatch(List<Notifiable> data, NotificationPolicy<Notifiable> policy) {
		List<SentNotifyMessage> result = new ArrayList<>();
		for (NotificationProvider<Notifiable> provider : providers) {
			result.addAll(provider.sendNotification(data, policy));
		}
		return result;
	}
}
