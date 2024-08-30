package co.fineants.api.domain.notification.domain.dto.request;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.notification.domain.entity.Notification;

public abstract class NotificationSaveRequest {

	public abstract Long getMemberId();

	public abstract Notification toEntity(Member member);
}
