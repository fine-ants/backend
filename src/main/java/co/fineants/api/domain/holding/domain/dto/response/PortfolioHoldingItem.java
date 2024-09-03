package co.fineants.api.domain.holding.domain.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioHoldingItem {

	@JsonUnwrapped
	private StockItem stock;
	@JsonUnwrapped
	private PortfolioHoldingDetailItem portfolioHolding;
	private List<PurchaseHistoryItem> purchaseHistory;

	public static PortfolioHoldingItem from(PortfolioHolding portfolioHolding, Expression lastDayClosingPrice) {
		StockItem stockItem = StockItem.from(portfolioHolding.getStock());
		PortfolioHoldingDetailItem holdingDetailItem = PortfolioHoldingDetailItem.from(portfolioHolding,
			lastDayClosingPrice);
		List<PurchaseHistoryItem> purchaseHistory = portfolioHolding.getPurchaseHistory().stream()
			.map(PurchaseHistoryItem::from)
			.toList();
		return new PortfolioHoldingItem(stockItem, holdingDetailItem, purchaseHistory);
	}
}
