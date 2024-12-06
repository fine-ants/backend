package co.fineants.api.domain.notification.service;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.fcm.service.FcmService;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessage;
import co.fineants.api.domain.notification.domain.entity.policy.NotificationPolicy;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotifyMessageFactory {

	private final FcmService fcmService;

	public List<NotifyMessage> generate(List<Notifiable> data,
		NotificationPolicy<Notifiable> policy) {
		return data.stream()
			.filter(policy::isSatisfied)
			.map(notifiable -> fcmService.findTokens(notifiable.fetchMemberId()).stream()
				.map(notifiable::createMessage)
				.toList())
			.flatMap(Collection::stream)
			.toList();
	}
}
