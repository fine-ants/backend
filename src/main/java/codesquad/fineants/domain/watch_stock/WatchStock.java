package codesquad.fineants.domain.watch_stock;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.watch_list.WatchList;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class WatchStock extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "watch_list_id")
	private WatchList watchList;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticker_symbol", referencedColumnName = "tickerSymbol")
	private Stock stock;

	@Builder
	public WatchStock(LocalDateTime createAt, LocalDateTime modifiedAt, Long id, WatchList watchList,
		Stock stock) {
		super(createAt, modifiedAt);
		this.id = id;
		this.watchList = watchList;
		this.stock = stock;
	}

	public static WatchStock create(WatchList watchList, Stock stock) {
		return new WatchStock(LocalDateTime.now(), LocalDateTime.now(), null, watchList, stock);
	}
}
