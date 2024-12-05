package co.fineants.api.domain.notification.domain.dto.response;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.util.Strings;
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
import lombok.experimental.SuperBuilder;

@Getter
@EqualsAndHashCode(of = {"referenceId", "memberId"})
@ToString
@SuperBuilder(toBuilder = true)
public abstract class NotifyMessage implements Comparable<NotifyMessage> {
	private final String title;
	private final String content;
	private final NotificationType type;
	private final String referenceId;
	private final Long memberId;
	private final String token;
	private final String link;
	private final List<String> messageIds;
	
	public static NotifyMessage portfolio(
		String title,
		String content,
		NotificationType type,
		String referenceId,
		Long memberId,
		String token,
		String link,
		String name,
		Long portfolioId) {
		return PortfolioNotifyMessage.builder()
			.title(title)
			.content(content)
			.type(type)
			.referenceId(referenceId)
			.memberId(memberId)
			.token(token)
			.link(link)
			.name(name)
			.portfolioId(portfolioId)
			.messageIds(new ArrayList<>())
			.build();
	}

	public static NotifyMessage stock(String title, String content, NotificationType type, String referenceId,
		Long memberId, String token, String link, String stockName, Money targetPrice, Long targetPriceNotificationId) {
		return StockNotifyMessage.builder()
			.title(title)
			.content(content)
			.type(type)
			.referenceId(referenceId)
			.memberId(memberId)
			.token(token)
			.link(link)
			.stockName(stockName)
			.targetPrice(targetPrice)
			.targetPriceNotificationId(targetPriceNotificationId)
			.messageIds(new ArrayList<>())
			.build();
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

	public boolean hasNotMessageId() {
		return messageIds.contains(Strings.EMPTY);
	}

	public abstract String getIdToSentHistory();

	public abstract NotifyMessage withMessageId(List<String> messageIds);

	public abstract co.fineants.api.domain.notification.domain.entity.Notification toEntity(Member member);

	@Override
	public int compareTo(@NotNull NotifyMessage notifyMessage) {
		return referenceId.compareToIgnoreCase(notifyMessage.referenceId);
	}
}
