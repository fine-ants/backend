package co.fineants.api.domain.member.domain.dto.request;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MemberNotificationAllDeleteRequest {
	@NotNull(message = "필수 정보입니다")
	@Size(min = 1, message = "삭제할 알람의 개수는 1개 이상이어야 합니다")
	@JsonProperty
	private List<Long> notificationIds;

	@JsonCreator
	public MemberNotificationAllDeleteRequest(@JsonProperty("notificationIds") List<Long> notificationIds) {
		this.notificationIds = notificationIds;
	}

	public List<Long> notificationIds() {
		return new ArrayList<>(notificationIds);
	}

	@Override
	public String toString() {
		return String.format("MemberNotificationAllDeleteRequest(notificationIds=%s)", notificationIds);
	}
}
