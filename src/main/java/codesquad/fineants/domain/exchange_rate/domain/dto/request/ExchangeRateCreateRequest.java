package codesquad.fineants.domain.exchange_rate.domain.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRateCreateRequest {
	private String code;
}
