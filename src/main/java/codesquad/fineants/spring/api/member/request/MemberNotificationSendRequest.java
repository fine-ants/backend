package codesquad.fineants.spring.api.member.request;

import javax.validation.constraints.NotBlank;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.notification.Notification;
import codesquad.fineants.domain.notification.PortfolioNotification;
import codesquad.fineants.domain.notification.PortfolioNotificationType;
import codesquad.fineants.domain.notification.StockTargetPriceNotification;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MemberNotificationSendRequest {
	@NotBlank(message = "필수 정보입니다")
	private String title;
	@NotBlank(message = "필수 정보입니다")
	private String name;
	@NotBlank(message = "필수 정보입니다")
	private String target;
	@NotBlank(message = "필수 정보입니다")
	private String type;
	@NotBlank(message = "필수 정보입니다")
	private String referenceId;

	public Notification toEntity(Member member) {
		if (type.equals("portfolio")) {

			return PortfolioNotification.builder()
				.title(title)
				.isRead(false)
				.type(type)
				.referenceId(referenceId)
				.member(member)
				.portfolioName(name)
				.notificationType(PortfolioNotificationType.from(target))
				.build();
		}
		return StockTargetPriceNotification.builder()
			.title(title)
			.isRead(false)
			.type(type)
			.referenceId(referenceId)
			.member(member)
			.stockName(name)
			.targetPrice(Long.valueOf(target))
			.build();
	}
}
