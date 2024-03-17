package codesquad.fineants.spring.api.stock_target_price.request;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.notification.Notification;
import codesquad.fineants.domain.notification.type.NotificationType;
import codesquad.fineants.spring.api.notification.response.NotifyMessage;
import codesquad.fineants.spring.api.notification.response.StockNotifyMessage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@ToString
public class StockNotificationRequest {
	private String stockName;
	private Long targetPrice;
	private String title;
	private NotificationType type;
	private String referenceId;
	private String link;
	private Long targetPriceNotificationId;
	private Long memberId;

	public static StockNotificationRequest from(NotifyMessage message) {
		StockNotifyMessage stock = (StockNotifyMessage)message;
		return StockNotificationRequest.builder()
			.stockName(stock.getStockName())
			.targetPrice(stock.getTargetPrice())
			.title(stock.getTitle())
			.type(stock.getType())
			.referenceId(stock.getReferenceId())
			.link(stock.getLink())
			.targetPriceNotificationId(stock.getTargetPriceNotificationId())
			.memberId(stock.getMemberId())
			.build();
	}

	public Notification toEntity(Member member) {
		return Notification.stock(
			stockName,
			targetPrice,
			title,
			referenceId,
			link,
			targetPriceNotificationId,
			member);
	}
}
