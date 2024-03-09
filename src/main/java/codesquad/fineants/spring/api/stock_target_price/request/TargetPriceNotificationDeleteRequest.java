package codesquad.fineants.spring.api.stock_target_price.request;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TargetPriceNotificationDeleteRequest {
	@NotNull(message = "필수 정보입니다")
	private String tickerSymbol;

	@NotNull(message = "필수 정보입니다")
	@Size(min = 1, message = "등록번호가 최소 1개 이상이어야 합니다")
	private List<Long> targetPriceNotificationIds;
}
