package codesquad.fineants.spring.api.stock_target_price.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.notification.Notification;
import codesquad.fineants.domain.notification.type.NotificationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class StockTargetPriceNotificationCreateRequest {
	@NotBlank(message = "필수 정보입니다")
	private String tickerSymbol;
	@NotNull(message = "필수 정보입니다")
	@PositiveOrZero(message = "지정가는 0포함 양수여야 합니다")
	private Long targetPrice;
	@NotBlank(message = "필수 정보입니다")
	private String title;
	@NotNull(message = "필수 정보입니다")
	private NotificationType type;
	@NotBlank(message = "필수 정보입니다")
	private String referenceId;

	public Notification toEntity(Member member) {
		return Notification.stockTargetPriceNotification(tickerSymbol, targetPrice, title, referenceId, member);
	}
}
