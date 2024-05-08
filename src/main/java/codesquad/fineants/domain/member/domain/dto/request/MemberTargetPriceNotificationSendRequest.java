package codesquad.fineants.domain.member.domain.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.notification.domain.entity.Notification;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MemberTargetPriceNotificationSendRequest {
	@NotBlank(message = "필수 정보입니다")
	private String title;
	@NotBlank(message = "필수 정보입니다")
	private String name;
	@NotNull
	@PositiveOrZero
	private Money target;
	@NotBlank(message = "필수 정보입니다")
	private String referenceId;
	@NotBlank(message = "필수 정보입니다")
	private String link;
	@Positive(message = "지정가 알림 등록번호는 양수여야 합니다")
	private Long targetPriceNotificationId;

	public Notification toEntity(Member member) {
		return Notification.stock(name, target, title, referenceId, link, targetPriceNotificationId, member);
	}
}
