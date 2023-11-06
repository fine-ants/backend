package codesquad.fineants.spring.api.portfolio_notification.request;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PortfolioNotificationModifyRequest {
	@JsonProperty("isActive")
	@NotNull(message = "활성화/비활성 정보는 필수정보입니다.")
	private Boolean isActive;
}
