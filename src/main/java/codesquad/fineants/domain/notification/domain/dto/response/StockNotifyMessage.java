package codesquad.fineants.domain.notification.domain.dto.response;

import java.util.Objects;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.notification.domain.entity.type.NotificationType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class StockNotifyMessage extends NotifyMessage {

	private final String stockName;
	private final Money targetPrice;
	private final Long targetPriceNotificationId;

	@Builder(access = AccessLevel.PRIVATE)
	public StockNotifyMessage(String title, String content, NotificationType type, String referenceId, Long memberId,
		String token, String link, String stockName, Money targetPrice, Long targetPriceNotificationId) {
		super(title, content, type, referenceId, memberId, token, link);
		this.stockName = stockName;
		this.targetPrice = targetPrice;
		this.targetPriceNotificationId = targetPriceNotificationId;
	}

	public static NotifyMessage create(String title, String content, NotificationType type, String referenceId,
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
			.build();
	}

	@Override
	public String getIdToSentHistory() {
		return String.format("targetPriceNotification:%d", targetPriceNotificationId);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object == null || getClass() != object.getClass())
			return false;
		if (!super.equals(object))
			return false;
		StockNotifyMessage that = (StockNotifyMessage)object;
		return Objects.equals(stockName, that.stockName) && Objects.equals(targetPrice,
			that.targetPrice) && Objects.equals(targetPriceNotificationId, that.targetPriceNotificationId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), stockName, targetPrice, targetPriceNotificationId);
	}
}
