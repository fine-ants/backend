package codesquad.fineants.domain.member.controller;

import static org.springframework.http.HttpStatus.*;

import java.time.LocalDateTime;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import codesquad.fineants.domain.member.domain.dto.request.LoginRequest;
import codesquad.fineants.domain.member.domain.dto.request.ModifyPasswordRequest;
import codesquad.fineants.domain.member.domain.dto.request.OauthMemberLogoutRequest;
import codesquad.fineants.domain.member.domain.dto.request.OauthMemberRefreshRequest;
import codesquad.fineants.domain.member.domain.dto.request.ProfileChangeRequest;
import codesquad.fineants.domain.member.domain.dto.request.ProfileChangeServiceRequest;
import codesquad.fineants.domain.member.domain.dto.request.SignUpRequest;
import codesquad.fineants.domain.member.domain.dto.request.SignUpServiceRequest;
import codesquad.fineants.domain.member.domain.dto.request.VerifyCodeRequest;
import codesquad.fineants.domain.member.domain.dto.request.VerifyEmailRequest;
import codesquad.fineants.domain.member.domain.dto.response.LoginResponse;
import codesquad.fineants.domain.member.domain.dto.response.OauthMemberRefreshResponse;
import codesquad.fineants.domain.member.domain.dto.response.ProfileChangeResponse;
import codesquad.fineants.domain.member.domain.dto.response.ProfileResponse;
import codesquad.fineants.domain.member.domain.dto.response.SignUpServiceResponse;
import codesquad.fineants.domain.member.service.MemberService;
import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.security.auth.dto.MemberAuthentication;
import codesquad.fineants.global.security.auth.resolver.MemberAuthenticationPrincipal;
import codesquad.fineants.global.success.MemberSuccessCode;
import codesquad.fineants.global.success.OauthSuccessCode;
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

	@GetMapping(value = "/auth/logout")
	public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		memberService.logout(request, response);
		return ApiResponse.success(OauthSuccessCode.OK_LOGOUT);
	}

	@ResponseStatus(OK)
	@PostMapping("/auth/refresh/token")
	public ApiResponse<OauthMemberRefreshResponse> refreshAccessToken(
		@RequestBody final OauthMemberRefreshRequest request
	) {
		OauthMemberRefreshResponse response = memberService.refreshAccessToken(request, LocalDateTime.now());
		return ApiResponse.success(OauthSuccessCode.OK_REFRESH_TOKEN, response);
	}

	@ResponseStatus(CREATED)
	@PostMapping(value = "/auth/signup", consumes = {MediaType.APPLICATION_JSON_VALUE,
		MediaType.MULTIPART_FORM_DATA_VALUE})
	public ApiResponse<Void> signup(
		@Valid @RequestPart(value = "signupData") SignUpRequest request,
		@RequestPart(value = "profileImageFile", required = false) MultipartFile profileImageFile
	) {
		SignUpServiceRequest serviceRequest = SignUpServiceRequest.of(request, profileImageFile);
		SignUpServiceResponse response = memberService.signup(serviceRequest);
		log.info("일반 회원 가입 컨트롤러 결과 : {}", response);
		return ApiResponse.success(MemberSuccessCode.OK_SIGNUP);
	}

	@PostMapping("/auth/signup/verifyEmail")
	public ApiResponse<Void> sendVerifyCode(@Valid @RequestBody VerifyEmailRequest request) {
		memberService.sendVerifyCode(request);
		return ApiResponse.success(MemberSuccessCode.OK_SEND_VERIFY_CODE);
	}

	@PostMapping("/auth/signup/verifyCode")
	public ApiResponse<Void> checkVerifyCode(@Valid @RequestBody VerifyCodeRequest request) {
		memberService.checkVerifyCode(request);
		return ApiResponse.success(MemberSuccessCode.OK_VERIF_CODE);
	}

	@GetMapping("/auth/signup/duplicationcheck/nickname/{nickname}")
	public ApiResponse<Void> nicknameDuplicationCheck(@PathVariable final String nickname) {
		memberService.checkNickname(nickname);
		return ApiResponse.success(MemberSuccessCode.OK_NICKNAME_CHECK);
	}

	@GetMapping("/auth/signup/duplicationcheck/email/{email}")
	public ApiResponse<Void> emailDuplicationCheck(@PathVariable final String email) {
		memberService.checkEmail(email);
		return ApiResponse.success(MemberSuccessCode.OK_EMAIL_CHECK);
	}

	@PostMapping("/auth/login")
	public ApiResponse<LoginResponse> login(@RequestBody final LoginRequest request) {
		return ApiResponse.success(MemberSuccessCode.OK_LOGIN, memberService.login(request));
	}

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
		@MemberAuthenticationPrincipal MemberAuthentication authentication
	) {
		memberService.deleteMember(authentication.getId());
		return ApiResponse.success(MemberSuccessCode.OK_DELETED_ACCOUNT);
	}
}
