package co.fineants.api.domain.member.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.domain.member.domain.entity.MemberProfile;
import jakarta.validation.constraints.Pattern;

public class ProfileChangeRequest {
	@Pattern(regexp = MemberProfile.NICKNAME_REGEXP, message = "잘못된 입력형식입니다.")
	@JsonProperty
	private final String nickname;

	@JsonCreator
	public ProfileChangeRequest(@JsonProperty("nickname") String nickname) {
		this.nickname = nickname;
	}

	public String nickname() {
		return nickname;
	}

	@Override
	public String toString() {
		return String.format("ProfileChangeRequest(nickname=%s)", nickname);
	}
}
