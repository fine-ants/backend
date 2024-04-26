package codesquad.fineants.spring.api.portfolio_notification.request;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class PortfolioNotificationUpdateRequest {
	@JsonProperty("isActive")
	@NotNull(message = "활성화/비활성 정보는 필수정보입니다.")
	private Boolean isActive;

	public static PortfolioNotificationUpdateRequest active() {
		return new PortfolioNotificationUpdateRequest(true);
	}

	public static PortfolioNotificationUpdateRequest inactive() {
		return new PortfolioNotificationUpdateRequest(false);
	}
}
