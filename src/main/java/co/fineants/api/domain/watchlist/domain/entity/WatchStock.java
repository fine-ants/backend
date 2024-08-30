package co.fineants.api.domain.watchlist.domain.entity;

import java.time.LocalDateTime;

import co.fineants.api.domain.BaseEntity;
import co.fineants.api.domain.stock.domain.entity.Stock;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
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

	private WatchStock(WatchList watchList, Stock stock) {
		this(LocalDateTime.now(), null, null, watchList, stock);
	}

	private WatchStock(LocalDateTime createAt, LocalDateTime modifiedAt, Long id, WatchList watchList,
		Stock stock) {
		super(createAt, modifiedAt);
		this.id = id;
		this.watchList = watchList;
		this.stock = stock;
	}

	public static WatchStock newWatchStock(WatchList watchList, Stock stock) {
		return new WatchStock(watchList, stock);
	}
}
