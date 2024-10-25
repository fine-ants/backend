package co.fineants.api.domain.member.domain.dto.request;

import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class ProfileChangeServiceRequest {
	private MultipartFile profileImageFile;
	private String nickname;
	private Long memberId;

	public static ProfileChangeServiceRequest of(MultipartFile profileImageFile, String nickname,
		Long memberId) {
		return new ProfileChangeServiceRequest(profileImageFile, nickname, memberId);
	}
}
