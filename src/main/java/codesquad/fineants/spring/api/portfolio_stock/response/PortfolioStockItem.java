package codesquad.fineants.spring.api.portfolio_stock.response;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioStockItem {

	@JsonUnwrapped
	private StockItem stock;
	@JsonUnwrapped
	private PortfolioHoldingDetailItem portfolioHolding;
	private List<PurchaseHistoryItem> purchaseHistory;

	public static PortfolioStockItem from(PortfolioHolding portfolioHolding, long lastDayClosingPrice) {
		StockItem stockItem = StockItem.from(portfolioHolding.getStock());
		PortfolioHoldingDetailItem holdingDetailItem = PortfolioHoldingDetailItem.from(portfolioHolding,
			lastDayClosingPrice);
		List<PurchaseHistoryItem> purchaseHistory = portfolioHolding.getPurchaseHistory().stream()
			.map(PurchaseHistoryItem::from)
			.collect(Collectors.toList());
		return new PortfolioStockItem(stockItem, holdingDetailItem, purchaseHistory);
	}
}
