package codesquad.fineants.domain.purchase_history;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.spring.api.portfolio_stock.request.PortfolioStockCreateRequest;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString(exclude = {"portfolioHolding"})
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseHistory extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private LocalDateTime purchaseDate;
	private Double purchasePricePerShare;
	private Long numShares;
	private String memo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "portfolio_holding_id")
	private PortfolioHolding portfolioHolding;

	@Builder
	public PurchaseHistory(Long id, LocalDateTime purchaseDate, Double purchasePricePerShare, Long numShares,
		String memo,
		PortfolioHolding portfolioHolding) {
		this.id = id;
		this.purchaseDate = purchaseDate;
		this.purchasePricePerShare = purchasePricePerShare;
		this.numShares = numShares;
		this.memo = memo;
		this.portfolioHolding = portfolioHolding;
	}

	public static PurchaseHistory of(PortfolioHolding portFolioHolding,
		PortfolioStockCreateRequest.PurchaseHistoryCreateRequest purchaseHistory) {
		return PurchaseHistory.builder()
			.portfolioHolding(portFolioHolding)
			.purchaseDate(purchaseHistory.getPurchaseDate())
			.purchasePricePerShare(purchaseHistory.getPurchasePricePerShare())
			.numShares(purchaseHistory.getNumShares())
			.memo(purchaseHistory.getMemo())
			.build();
	}

	// 투자 금액 = 주당 매입가 * 개수
	public Long calculateInvestmentAmount() {
		return purchasePricePerShare.longValue() * numShares;
	}

	// 손익 = (현재가 - 평균 매입가) * 주식개수
	public Long calculateGain() {
		return (portfolioHolding.getCurrentPrice() - purchasePricePerShare.longValue()) * numShares;
	}

	public PurchaseHistory change(PurchaseHistory history) {
		this.purchaseDate = history.getPurchaseDate();
		this.purchasePricePerShare = history.getPurchasePricePerShare();
		this.numShares = history.getNumShares();
		this.memo = history.getMemo();
		return this;
	}

	public LocalDate getPurchaseLocalDate() {
		return purchaseDate.toLocalDate();
	}

	// 배당금을 받을 수 있는지 검사
	public boolean isSatisfiedDividend(LocalDate exDividendDate) {
		LocalDate purcahseLocalDate = purchaseDate.toLocalDate();
		return purcahseLocalDate.equals(exDividendDate)
			|| purcahseLocalDate.isBefore(exDividendDate);
	}

}
