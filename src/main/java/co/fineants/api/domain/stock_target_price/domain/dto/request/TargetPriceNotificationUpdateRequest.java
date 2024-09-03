package co.fineants.api.domain.stock_target_price.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TargetPriceNotificationUpdateRequest {
	@NotBlank(message = "필수 정보입니다")
	private String tickerSymbol;
	@NotNull(message = "필수 정보입니다")
	private Boolean isActive;
}
