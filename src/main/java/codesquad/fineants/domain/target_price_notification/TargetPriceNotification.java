package codesquad.fineants.domain.target_price_notification;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.notification.type.NotificationType;
import codesquad.fineants.domain.stock_target_price.StockTargetPrice;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.notification.response.NotifyMessage;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "target_price_notification")
public class TargetPriceNotification extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long targetPrice;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stock_target_price_id")
	private StockTargetPrice stockTargetPrice;

	@Builder
	public TargetPriceNotification(LocalDateTime createAt, LocalDateTime modifiedAt, Long id,
		Long targetPrice, StockTargetPrice stockTargetPrice) {
		super(createAt, modifiedAt);
		this.id = id;
		this.targetPrice = targetPrice;
		this.stockTargetPrice = stockTargetPrice;
	}

	public boolean isMatchMember(Long memberId) {
		return stockTargetPrice.getMember().hasAuthorization(memberId);
	}

	public String getReferenceId() {
		return stockTargetPrice.getStock().getTickerSymbol();
	}

	public boolean isActive() {
		return stockTargetPrice.getIsActive();
	}

	public NotifyMessage getTargetPriceMessage(String token) {
		NotificationType type = NotificationType.STOCK_TARGET_PRICE;
		String title = type.getName();
		String content = String.format("%s이(가) %d 금액에 도달했습니다", stockTargetPrice.getStock().getCompanyName(),
			targetPrice);
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

	public boolean isSameTargetPrice(CurrentPriceManager manager) {
		Long currentPrice = stockTargetPrice.getCurrentPrice(manager);
		return targetPrice.equals(currentPrice);
	}
}
