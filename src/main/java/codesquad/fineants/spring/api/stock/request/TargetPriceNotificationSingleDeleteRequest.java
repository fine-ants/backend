package codesquad.fineants.spring.api.stock.request;

import javax.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TargetPriceNotificationSingleDeleteRequest {
	@NotNull(message = "필수 정보입니다")
	private String tickerSymbol;
}
