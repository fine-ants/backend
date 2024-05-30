package codesquad.fineants.domain.notification.domain.entity;

import java.time.LocalDateTime;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.MoneyConverter;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.notification.domain.entity.type.NotificationType;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("S")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class StockTargetPriceNotification extends Notification {
	private String stockName;
	@Convert(converter = MoneyConverter.class)
	private Money targetPrice;
	private Long targetPriceNotificationId;

	@Builder
	public StockTargetPriceNotification(LocalDateTime createAt, LocalDateTime modifiedAt, Long id,
		String title, Boolean isRead, NotificationType type, String referenceId, String link, Member member,
		String stockName, Money targetPrice, Long targetPriceNotificationId) {
		super(createAt, modifiedAt, id, title, isRead, type, referenceId, link, member);
		this.stockName = stockName;
		this.targetPrice = targetPrice;
		this.targetPriceNotificationId = targetPriceNotificationId;
	}

	public static StockTargetPriceNotification create(String stockName, Money targetPrice, String title,
		String referenceId, String link, Long targetPriceNotificationId, Member member) {
		return StockTargetPriceNotification.builder()
			.stockName(stockName)
			.targetPrice(targetPrice)
			.title(title)
			.isRead(false)
			.type(NotificationType.STOCK_TARGET_PRICE)
			.referenceId(referenceId)
			.link(link)
			.member(member)
			.targetPriceNotificationId(targetPriceNotificationId)
			.build();
	}

	@Override
	public NotificationBody getBody() {
		return NotificationBody.stock(stockName, targetPrice);
	}

	@Override
	public String getContent() {
		return String.format("%s이(가) %s%s에 도달했습니다", stockName, targetPrice.getCurrencySymbol(),
			targetPrice.toDecimalFormat());
	}

	@Override
	public String getName() {
		return stockName;
	}
}
