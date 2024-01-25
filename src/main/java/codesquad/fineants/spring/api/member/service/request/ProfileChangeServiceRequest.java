package codesquad.fineants.spring.api.member.service.request;

import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.spring.api.member.request.ProfileChangeRequest;
import lombok.Getter;

@Getter
public class ProfileChangeServiceRequest {
	private final Optional<MultipartFile> profileImageFile;
	private final Optional<ProfileChangeRequest> request;
	private final Optional<Long> memberId;

	public ProfileChangeServiceRequest(MultipartFile profileImageFile, ProfileChangeRequest request,
		AuthMember authMember) {
		this.profileImageFile = Optional.ofNullable(profileImageFile);
		this.request = Optional.ofNullable(request);
		this.memberId = Optional.of(authMember.getMemberId());
	}
}
