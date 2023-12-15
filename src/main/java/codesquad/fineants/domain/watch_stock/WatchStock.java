package codesquad.fineants.domain.watch_stock;

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
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "id", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "watch_list_id")
	private WatchList watchList;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticker_symbol", referencedColumnName = "tickerSymbol")
	private Stock stock;

	@Builder
	public WatchStock(WatchList watchList, Stock stock){
		this.watchList = watchList;
		this.stock = stock;
	}
}
