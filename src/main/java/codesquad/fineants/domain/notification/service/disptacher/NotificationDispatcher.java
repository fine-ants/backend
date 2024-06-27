package codesquad.fineants.domain.notification.service.disptacher;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import codesquad.fineants.domain.common.notification.Notifiable;
import codesquad.fineants.domain.notification.domain.dto.response.SentNotifyMessage;
import codesquad.fineants.domain.notification.domain.entity.policy.NotificationPolicy;
import codesquad.fineants.domain.notification.service.provider.NotificationProvider;
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
