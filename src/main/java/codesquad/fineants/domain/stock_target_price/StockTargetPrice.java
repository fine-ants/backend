package codesquad.fineants.domain.stock_target_price;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotification;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"member", "stock", "targetPriceNotifications"})
@Entity
@Table(name = "stock_target_price", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"member_id", "ticker_symbol"})
})
public class StockTargetPrice extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Boolean isActive;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticker_symbol", referencedColumnName = "tickerSymbol")
	private Stock stock;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "stockTargetPrice")
	private List<TargetPriceNotification> targetPriceNotifications;

	@Builder
	public StockTargetPrice(LocalDateTime createAt, LocalDateTime modifiedAt, Long id, Boolean isActive, Member member,
		Stock stock, List<TargetPriceNotification> targetPriceNotifications) {
		super(createAt, modifiedAt);
		this.id = id;
		this.isActive = isActive;
		this.member = member;
		this.stock = stock;
		this.targetPriceNotifications = targetPriceNotifications;
	}

	public void changeIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Long getCurrentPrice(CurrentPriceManager manager) {
		return stock.getCurrentPrice(manager);
	}
}
