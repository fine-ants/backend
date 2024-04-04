package codesquad.fineants.spring.api.stock_target_price.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.stock_target_price.StockTargetPrice;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class TargetPriceNotificationSearchItem {
	private String companyName;
	private String tickerSymbol;
	private Long lastPrice;
	private List<TargetPriceItem> targetPrices;
	private Boolean isActive;
	private LocalDateTime lastUpdated;

	public static TargetPriceNotificationSearchItem from(StockTargetPrice stockTargetPrice,
		LastDayClosingPriceManager manager) {

		List<TargetPriceItem> targetPrices = stockTargetPrice.getTargetPriceNotifications().stream()
			.map(TargetPriceItem::from)
			.collect(Collectors.toList());

		return TargetPriceNotificationSearchItem.builder()
			.companyName(stockTargetPrice.getStock().getCompanyName())
			.tickerSymbol(stockTargetPrice.getStock().getTickerSymbol())
			.lastPrice(manager.getPrice(stockTargetPrice.getStock().getTickerSymbol())
				.orElse(Money.zero())
				.getAmount().longValue())
			.targetPrices(targetPrices)
			.isActive(stockTargetPrice.getIsActive())
			.lastUpdated(stockTargetPrice.getModifiedAt())
			.build();
	}
}
