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
import codesquad.fineants.domain.stock_target_price.StockTargetPrice;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

	public boolean isMatchTickerSymbol(String tickerSymbol) {
		return stockTargetPrice.getStock().getTickerSymbol().equals(tickerSymbol);
	}
}
