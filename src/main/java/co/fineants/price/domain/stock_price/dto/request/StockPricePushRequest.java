package co.fineants.price.domain.stock_price.dto.request;

import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StockPricePushRequest {
	private List<String> tickerSymbols;
}
