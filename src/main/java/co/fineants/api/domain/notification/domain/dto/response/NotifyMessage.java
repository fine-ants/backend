package co.fineants.api.domain.notification.domain.dto.response;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushFcmOptions;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.fcm.service.FcmService;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.notification.domain.dto.request.NotificationSaveRequest;
import co.fineants.api.domain.notification.domain.dto.request.PortfolioNotificationSaveRequest;
import co.fineants.api.domain.notification.domain.entity.type.NotificationType;
import co.fineants.api.domain.stock_target_price.domain.dto.request.StockNotificationSaveRequest;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(of = {"referenceId", "memberId"})
@ToString
public abstract class NotifyMessage implements Comparable<NotifyMessage> {
	private final String title;
	private final String content;
	private final NotificationType type;
	private final String referenceId;
	private final Long memberId;
	private final String token;
	private final String link;
	private final List<String> messageIds;

	protected NotifyMessage(String title, String content, NotificationType type, String referenceId, Long memberId,
		String token, String link, List<String> messageIds) {
		this.title = title;
		this.content = content;
		this.type = type;
		this.referenceId = referenceId;
		this.memberId = memberId;
		this.token = token;
		this.link = link;
		this.messageIds = messageIds;
	}

	public static NotifyMessage portfolio(String title, String content, NotificationType type, String referenceId,
		Long memberId, String token, String link, String name) {
		return portfolio(title, content, type, referenceId, memberId, token, link, name, new ArrayList<>());
	}

	public static NotifyMessage portfolio(String title, String content, NotificationType type, String referenceId,
		Long memberId, String token, String link, String name, List<String> messageIds) {
		return PortfolioNotifyMessage.create(title, content, type, referenceId, memberId, token, link, name,
			messageIds);
	}

	public static NotifyMessage stock(String title, String content, NotificationType type, String referenceId,
		Long memberId, String token, String link, String stockName, Money targetPrice, Long targetPriceNotificationId) {
		return stock(title, content, type, referenceId, memberId, token, link, stockName,
			targetPrice, targetPriceNotificationId, null);
	}

	public static NotifyMessage stock(String title, String content, NotificationType type, String referenceId,
		Long memberId, String token, String link, String stockName, Money targetPrice, Long targetPriceNotificationId,
		List<String> messageIds) {
		return StockNotifyMessage.create(title, content, type, referenceId, memberId, token, link, stockName,
			targetPrice, targetPriceNotificationId, messageIds);
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

	public void deleteTokenBy(FcmService service) {
		service.deleteToken(token);
	}

	public NotificationSaveRequest toNotificationSaveRequest() {
		if (type == NotificationType.PORTFOLIO_TARGET_GAIN || type == NotificationType.PORTFOLIO_MAX_LOSS) {
			return PortfolioNotificationSaveRequest.from(this);
		} else {
			return StockNotificationSaveRequest.from(this);
		}
	}

	public boolean hasMessageId() {
		return messageIds != null;
	}

	public abstract String getIdToSentHistory();

	public abstract NotifyMessage withMessageId(List<String> messageIds);

	public abstract co.fineants.api.domain.notification.domain.entity.Notification toEntity(Member member);

	@Override
	public int compareTo(@NotNull NotifyMessage notifyMessage) {
		return referenceId.compareToIgnoreCase(notifyMessage.referenceId);
	}
}
