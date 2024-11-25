package co.fineants.api.domain.stock_target_price.domain.entity;

import java.time.LocalDateTime;

import co.fineants.api.domain.BaseEntity;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.MoneyConverter;
import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessage;
import co.fineants.api.domain.notification.domain.entity.type.NotificationType;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "target_price_notification")
public class TargetPriceNotification extends BaseEntity implements Notifiable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Convert(converter = MoneyConverter.class)
	@Column(precision = 19, nullable = false)
	private Money targetPrice;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stock_target_price_id")
	private StockTargetPrice stockTargetPrice;

	private TargetPriceNotification(LocalDateTime createAt, LocalDateTime modifiedAt, Long id,
		Money targetPrice, StockTargetPrice stockTargetPrice) {
		super(createAt, modifiedAt);
		this.id = id;
		this.targetPrice = targetPrice;
		this.stockTargetPrice = stockTargetPrice;
	}

	public static TargetPriceNotification newTargetPriceNotification(Money targetPrice,
		StockTargetPrice stockTargetPrice) {
		return newTargetPriceNotification(null, targetPrice, stockTargetPrice);
	}

	public static TargetPriceNotification newTargetPriceNotification(Long id, Money targetPrice,
		StockTargetPrice stockTargetPrice) {
		return new TargetPriceNotification(LocalDateTime.now(), null, id, targetPrice, stockTargetPrice);
	}

	public String getReferenceId() {
		return stockTargetPrice.getStock().getTickerSymbol();
	}

	public boolean isActive() {
		return stockTargetPrice.getIsActive();
	}

	public boolean isSameTargetPrice(CurrentPriceRedisRepository manager) {
		Expression currentPrice = stockTargetPrice.getCurrentPrice(manager);
		return targetPrice.compareTo(currentPrice) == 0;
	}

	@Override
	public Long fetchMemberId() {
		return stockTargetPrice.getMember().getId();
	}

	@Override
	public NotifyMessage createTargetGainMessageWith(String token) {
		throw new UnsupportedOperationException("This method is not supported for TargetPriceNotification");
	}

	@Override
	public NotificationPreference getNotificationPreference() {
		return stockTargetPrice.getMember().getNotificationPreference();
	}

	@Override
	public NotifyMessage createMaxLossMessageWith(String token) {
		throw new UnsupportedOperationException("This method is not supported for TargetPriceNotification");
	}

	@Override
	public NotifyMessage createTargetPriceMessage(String token) {
		NotificationType type = NotificationType.STOCK_TARGET_PRICE;
		String title = type.getName();
		String content = String.format("%s이(가) %s%s에 도달했습니다",
			stockTargetPrice.getStock().getCompanyName(),
			targetPrice.currencySymbol(),
			targetPrice.toDecimalFormat());
		String referenceId = stockTargetPrice.getStock().getTickerSymbol();
		Long memberId = stockTargetPrice.getMember().getId();
		String link = "/stock/" + referenceId;
		String stockName = stockTargetPrice.getStock().getCompanyName();
		return NotifyMessage.stock(
			title,
			content,
			type,
			referenceId,
			memberId,
			token,
			link,
			stockName,
			targetPrice,
			id
		);
	}

	public boolean hasAuthorization(Long memberId) {
		return stockTargetPrice.hasAuthorization(memberId);
	}
}
