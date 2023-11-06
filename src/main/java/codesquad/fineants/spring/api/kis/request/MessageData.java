package codesquad.fineants.spring.api.kis.request;

import java.util.List;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class MessageData {
	private List<String> tickerSymbols;
}
