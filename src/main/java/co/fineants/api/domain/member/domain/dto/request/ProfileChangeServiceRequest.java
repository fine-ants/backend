package co.fineants.api.domain.member.domain.dto.request;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.jsonwebtoken.lang.Strings;
import lombok.Getter;

public class ProfileChangeServiceRequest {
	@JsonProperty
	@Getter
	private final MultipartFile profileImageFile;
	@JsonProperty
	private final String nickname;
	@JsonProperty
	private final Long memberId;

	@JsonCreator
	public ProfileChangeServiceRequest(
		@JsonProperty("profileImageFile") MultipartFile profileImageFile,
		@JsonProperty("nickname") String nickname,
		@JsonProperty("memberId") Long memberId) {
		this.profileImageFile = profileImageFile;
		this.nickname = nickname;
		this.memberId = memberId;
	}

	public static ProfileChangeServiceRequest of(MultipartFile profileImageFile, String nickname,
		Long memberId) {
		return new ProfileChangeServiceRequest(profileImageFile, nickname, memberId);
	}

	public boolean hasNickname() {
		return Strings.hasText(nickname);
	}

	public Long memberId() {
		return memberId;
	}

	public String nickname() {
		return nickname;
	}

	@Override
	public String toString() {
		return String.format("ProfileChangeServiceRequest(nickname=%s, memberId=%d)", nickname, memberId);
	}
}
