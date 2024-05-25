package codesquad.fineants.domain.exchangerate.domain.dto.response;

import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRateDeleteRequest {
	private List<String> codes;
}
