package codesquad.fineants.domain.notification.domain.dto.response;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushFcmOptions;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.notification.domain.entity.type.NotificationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@EqualsAndHashCode(of = {"referenceId", "memberId"})
@ToString
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
		Long memberId, String token, String link, String stockName, Money targetPrice, Long targetPriceNotificationId) {
		return StockNotifyMessage.create(title, content, type, referenceId, memberId, token, link, stockName,
			targetPrice, targetPriceNotificationId);
	}

	public Message toMessage() {
		Notification notification = Notification.builder()
			.setTitle(title)
			.setBody(content)
			.build();
		return Message.builder()
			.setToken(token)
			.setNotification(notification)
			.setWebpushConfig(WebpushConfig.builder()
				.setFcmOptions(WebpushFcmOptions.builder()
					.setLink(link)
					.build())
				.build())
			.build();
	}
}
