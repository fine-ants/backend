package co.fineants.api.domain.stock_target_price.domain.dto.request;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.notification.domain.dto.request.NotificationSaveRequest;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessage;
import co.fineants.api.domain.notification.domain.dto.response.StockNotifyMessage;
import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.domain.entity.type.NotificationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
public class StockNotificationSaveRequest extends NotificationSaveRequest {
	private String stockName;
	@NotNull
	@PositiveOrZero
	private Money targetPrice;
	private String title;
	private NotificationType type;
	private String referenceId;
	private String link;
	private Long targetPriceNotificationId;
	private Long memberId;

	public static StockNotificationSaveRequest from(NotifyMessage message) {
		StockNotifyMessage stock = (StockNotifyMessage)message;
		return StockNotificationSaveRequest.builder()
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

	@Override
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
