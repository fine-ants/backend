package codesquad.fineants.domain.notification.domain.dto.request;

import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.notification.domain.entity.Notification;

public abstract class NotificationSaveRequest {

	public abstract Long getMemberId();

	public abstract Notification toEntity(Member member);
}
