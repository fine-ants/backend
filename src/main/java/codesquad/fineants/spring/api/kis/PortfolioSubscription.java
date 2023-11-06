package codesquad.fineants.spring.api.kis;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(of = "portfolioId")
@ToString
public class PortfolioSubscription {
	private Long portfolioId;
	private List<String> tickerSymbols;
}
