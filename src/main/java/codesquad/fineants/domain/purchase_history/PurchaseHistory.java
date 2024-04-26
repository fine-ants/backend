package codesquad.fineants.domain.purchase_history;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.count.CountConverter;
import codesquad.fineants.domain.common.money.Bank;
import codesquad.fineants.domain.common.money.Expression;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.MoneyConverter;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.spring.api.purchase_history.request.PurchaseHistoryCreateRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString(exclude = {"portfolioHolding"})
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PurchaseHistory extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDateTime purchaseDate;

	@Convert(converter = MoneyConverter.class)
	@Column(precision = 19, nullable = false)
	private Money purchasePricePerShare;

	@Convert(converter = CountConverter.class)
	private Count numShares;

	private String memo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "portfolio_holding_id")
	private PortfolioHolding portfolioHolding;

	public static PurchaseHistory of(PortfolioHolding portFolioHolding, PurchaseHistoryCreateRequest request) {
		return new PurchaseHistory(
			null,
			request.getPurchaseDate(),
			request.getPurchasePricePerShare(),
			request.getNumShares(),
			request.getMemo(),
			portFolioHolding
		);
	}

	public static PurchaseHistory now(Money purchasePricePerShare, Count numShares, String memo,
		PortfolioHolding holding) {
		return new PurchaseHistory(
			null,
			LocalDateTime.now(),
			purchasePricePerShare,
			numShares,
			memo,
			holding
		);
	}

	// 투자 금액 = 주당 매입가 * 개수
	public Expression calculateInvestmentAmount() {
		Money money = purchasePricePerShare.multiply(numShares);
		return Bank.getInstance().reduce(money, money.currency());
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
