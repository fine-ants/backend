package codesquad.fineants.spring.api.member.response;

import java.util.List;

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
public class MemberNotificationResponse {
	private List<MemberNotification> notifications;

	public static MemberNotificationResponse create(List<MemberNotification> notifications) {
		return MemberNotificationResponse.builder()
			.notifications(notifications)
			.build();
	}
}
