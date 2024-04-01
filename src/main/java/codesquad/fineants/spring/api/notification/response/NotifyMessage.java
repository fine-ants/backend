package codesquad.fineants.spring.api.notification.response;

import java.util.Map;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushFcmOptions;

import codesquad.fineants.domain.notification.type.NotificationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@EqualsAndHashCode(of = {"referenceId", "memberId"})
public abstract class NotifyMessage {
	private String title;
	private String content;
	private NotificationType type;
	private String referenceId;
	private Long memberId;
	private String token;
	private String link;

	public static NotifyMessage portfolio(String title, String content, NotificationType type, String referenceId,
		Long memberId, String token, String link, String name) {
		return PortfolioNotifyMessage.create(title, content, type, referenceId, memberId, token, link, name);
	}

	public static NotifyMessage stock(String title, String content, NotificationType type, String referenceId,
		Long memberId, String token, String link, String stockName, Long targetPrice, Long targetPriceNotificationId) {
		return StockNotifyMessage.create(title, content, type, referenceId, memberId, token, link, stockName,
			targetPrice, targetPriceNotificationId);
	}

	public Message toMessage() {
		Map<String, String> data = Map.of(
			"title", title,
			"body", content
		);
		return Message.builder()
			.setToken(token)
			.putAllData(data)
			.setWebpushConfig(WebpushConfig.builder()
				.setFcmOptions(WebpushFcmOptions.builder()
					.setLink(link)
					.build())
				.build())
			.build();
	}
}
