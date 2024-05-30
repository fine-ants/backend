package codesquad.fineants.domain.stock_target_price.domain.entity;

import java.time.LocalDateTime;
import java.util.List;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.common.money.Expression;
import codesquad.fineants.domain.kis.repository.CurrentPriceRepository;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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

	public Expression getCurrentPrice(CurrentPriceRepository manager) {
		return stock.getCurrentPrice(manager);
	}
}
