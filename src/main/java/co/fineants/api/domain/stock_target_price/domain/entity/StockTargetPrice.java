package co.fineants.api.domain.stock_target_price.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.fineants.api.domain.BaseEntity;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.stock.domain.entity.Stock;
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

	private StockTargetPrice(LocalDateTime createAt, LocalDateTime modifiedAt, Long id, Boolean isActive, Member member,
		Stock stock, List<TargetPriceNotification> targetPriceNotifications) {
		super(createAt, modifiedAt);
		this.id = id;
		this.isActive = isActive;
		this.member = member;
		this.stock = stock;
		this.targetPriceNotifications = targetPriceNotifications;
	}

	public static StockTargetPrice newStockTargetPriceWithActive(Member member, Stock stock) {
		return newStockTargetPriceWithActive(null, member, stock);
	}

	public static StockTargetPrice newStockTargetPriceWithActive(Long id, Member member, Stock stock) {
		return new StockTargetPrice(LocalDateTime.now(), null, id, true, member, stock, new ArrayList<>());
	}

	public void changeIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Expression getCurrentPrice(CurrentPriceRedisRepository manager) {
		return stock.getCurrentPrice(manager);
	}

	public boolean hasAuthorization(Long memberId) {
		return member.hasAuthorization(memberId);
	}

	public List<TargetPriceNotification> getTargetPriceNotifications() {
		return Collections.unmodifiableList(targetPriceNotifications);
	}

	public String getReferenceId() {
		return stock.getTickerSymbol();
	}

	public Long getMemberId() {
		return member.getId();
	}

	public String getLink() {
		return "/stock/" + getReferenceId();
	}

	public String getName() {
		return stock.getCompanyName();
	}
}
