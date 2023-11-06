package codesquad.fineants.spring.api.portfolio_stock.request;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PortfolioStockCreateRequest {
	@NotBlank(message = "티커심볼은 필수 정보입니다")
	private String tickerSymbol;
}
