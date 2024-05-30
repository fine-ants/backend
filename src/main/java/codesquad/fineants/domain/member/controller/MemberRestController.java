package codesquad.fineants.domain.member.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import codesquad.fineants.domain.member.domain.dto.request.ModifyPasswordRequest;
import codesquad.fineants.domain.member.domain.dto.request.OauthMemberLogoutRequest;
import codesquad.fineants.domain.member.domain.dto.request.ProfileChangeRequest;
import codesquad.fineants.domain.member.domain.dto.request.ProfileChangeServiceRequest;
import codesquad.fineants.domain.member.domain.dto.response.ProfileChangeResponse;
import codesquad.fineants.domain.member.domain.dto.response.ProfileResponse;
import codesquad.fineants.domain.member.service.MemberService;
import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.security.oauth.dto.MemberAuthentication;
import codesquad.fineants.global.security.oauth.resolver.MemberAuthenticationPrincipal;
import codesquad.fineants.global.success.MemberSuccessCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/api")
@RestController
public class MemberRestController {

	private final MemberService memberService;

	@PostMapping(value = "/profile", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
	public ApiResponse<ProfileChangeResponse> changeProfile(
		@RequestPart(value = "profileImageFile", required = false) MultipartFile profileImageFile,
		@Valid @RequestPart(value = "profileInformation", required = false) ProfileChangeRequest request,
		@MemberAuthenticationPrincipal MemberAuthentication authentication
	) {
		ProfileChangeServiceRequest serviceRequest = ProfileChangeServiceRequest.of(
			profileImageFile,
			request,
			authentication.getId()
		);
		return ApiResponse.success(MemberSuccessCode.OK_MODIFIED_PROFILE,
			memberService.changeProfile(serviceRequest));
	}

	@GetMapping(value = "/profile")
	public ApiResponse<ProfileResponse> readProfile(
		@MemberAuthenticationPrincipal MemberAuthentication authentication) {
		Long memberId = authentication.getId();
		return ApiResponse.success(MemberSuccessCode.OK_READ_PROFILE,
			memberService.readProfile(memberId));
	}

	@PutMapping("/account/password")
	public ApiResponse<Void> changePassword(
		@RequestBody ModifyPasswordRequest request,
		@MemberAuthenticationPrincipal MemberAuthentication authentication
	) {
		memberService.modifyPassword(request, authentication.getId());
		return ApiResponse.success(MemberSuccessCode.OK_PASSWORD_CHANGED);
	}

	@DeleteMapping("/account")
	public ApiResponse<Void> deleteAccount(
		@RequestBody OauthMemberLogoutRequest request,
		@MemberAuthenticationPrincipal MemberAuthentication authentication,
		HttpServletRequest servletRequest,
		HttpServletResponse servletResponse
	) {
		memberService.deleteMember(authentication.getId());
		memberService.logout(request, servletRequest, servletResponse);
		return ApiResponse.success(MemberSuccessCode.OK_DELETED_ACCOUNT);
	}
}
