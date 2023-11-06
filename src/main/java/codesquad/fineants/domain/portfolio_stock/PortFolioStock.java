package codesquad.fineants.domain.portfolio_stock;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.stock.Stock;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortFolioStock extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Long numberOfShares; // 주식 개수
	private Long totalGain; // 총 손익
	private Long annualDividend; // 연간배당금
	private Long currentValue; // 평가금액
	private Long currentPrice; // 현재가

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "portfolio_id")
	private Portfolio portfolio;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stock_id")
	private Stock stock;

	@Builder
	public PortFolioStock(Long id, Long numberOfShares, Long totalGain, Long annualDividend, Long currentValue,
		Long currentPrice, Portfolio portfolio, Stock stock) {
		this.id = id;
		this.numberOfShares = numberOfShares;
		this.totalGain = totalGain;
		this.annualDividend = annualDividend;
		this.currentValue = currentValue;
		this.currentPrice = currentPrice;
		this.portfolio = portfolio;
		this.stock = stock;
	}

	public Long calculateTotalCurrentPrice() {
		return currentPrice * numberOfShares;
	}

}
