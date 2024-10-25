package co.fineants.api.domain.member.domain.dto.request;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.MemberProfile;

public class SignUpServiceRequest {
	@JsonProperty
	private final String nickname;
	@JsonProperty
	private final String email;
	@JsonProperty
	private final String password;
	@JsonProperty
	private final String passwordConfirm;
	@JsonProperty
	private final MultipartFile profileImageFile;

	@JsonCreator
	private SignUpServiceRequest(
		@JsonProperty("nickname") String nickname,
		@JsonProperty("email") String email,
		@JsonProperty("password") String password,
		@JsonProperty("passwordConfirm") String passwordConfirm,
		@JsonProperty("profileImageFile") MultipartFile profileImageFile) {
		this.nickname = nickname;
		this.email = email;
		this.password = password;
		this.passwordConfirm = passwordConfirm;
		this.profileImageFile = profileImageFile;
	}

	public static SignUpServiceRequest create(String nickname, String email, String password, String passwordConfirm,
		MultipartFile profileImageFile) {
		return new SignUpServiceRequest(nickname, email, password, passwordConfirm, profileImageFile);
	}

	public static SignUpServiceRequest of(SignUpRequest request, MultipartFile profileImageFile) {
		return request.toSignUpServiceRequest(profileImageFile);
	}

	public Member toEntity() {
		MemberProfile profile = MemberProfile.localMemberProfile(email, nickname, password, null);
		return Member.localMember(profile);
	}

	public Member toEntity(String profileUrl, String encodedPassword) {
		MemberProfile profile = MemberProfile.localMemberProfile(email, nickname, encodedPassword, profileUrl);
		return Member.localMember(profile);
	}

	public boolean matchPassword() {
		return password.equals(passwordConfirm);
	}

	public boolean hasProfileImageFile() {
		return profileImageFile != null && !profileImageFile.isEmpty();
	}

	public MultipartFile profileImageFile() {
		return profileImageFile;
	}

	public String encodePasswordBy(PasswordEncoder encoder) {
		return encoder.encode(password);
	}
}
