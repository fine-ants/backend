package codesquad.fineants.spring.api.member.controller;

import static org.springframework.http.HttpStatus.*;

import java.time.LocalDateTime;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalMember;
import codesquad.fineants.spring.api.member.request.LoginRequest;
import codesquad.fineants.spring.api.member.request.ModifyPasswordRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberLoginRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberLogoutRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberRefreshRequest;
import codesquad.fineants.spring.api.member.request.ProfileChangeRequest;
import codesquad.fineants.spring.api.member.request.SignUpRequest;
import codesquad.fineants.spring.api.member.request.VerifCodeRequest;
import codesquad.fineants.spring.api.member.request.VerifyEmailRequest;
import codesquad.fineants.spring.api.member.response.LoginResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberLoginResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberRefreshResponse;
import codesquad.fineants.spring.api.member.response.OauthSaveUrlResponse;
import codesquad.fineants.spring.api.member.response.ProfileChangeResponse;
import codesquad.fineants.spring.api.member.service.MemberService;
import codesquad.fineants.spring.api.response.ApiResponse;
import codesquad.fineants.spring.api.success.code.MemberSuccessCode;
import codesquad.fineants.spring.api.success.code.OauthSuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/api")
@RestController
public class MemberRestController {

	private final MemberService memberService;

	@PostMapping("/auth/{provider}/authUrl")
	public ApiResponse<OauthSaveUrlResponse> saveAuthorizationCodeURL(@PathVariable final String provider) {
		return ApiResponse.success(OauthSuccessCode.OK_URL, memberService.saveAuthorizationCodeURL(provider));
	}

	@PostMapping(value = "/auth/{provider}/login")
	public ApiResponse<OauthMemberLoginResponse> login(@PathVariable final String provider,
		@RequestParam final String code, @RequestParam final String redirectUrl, @RequestParam final String state) {
		log.info("로그인 컨트롤러 요청 : provider = {}, code = {}, redirectUrl = {}, state = {}", provider, code, redirectUrl,
			state);
		return ApiResponse.success(OauthSuccessCode.OK_LOGIN,
			memberService.login(OauthMemberLoginRequest.of(provider, code, redirectUrl, state, LocalDateTime.now())));
	}

	@PostMapping(value = "/auth/logout")
	public ApiResponse<Void> logout(@RequestAttribute final String accessToken,
		@RequestBody final OauthMemberLogoutRequest request) {
		memberService.logout(accessToken, request);
		return ApiResponse.success(OauthSuccessCode.OK_LOGOUT);
	}

	@ResponseStatus(OK)
	@PostMapping("/auth/refresh/token")
	public ApiResponse<OauthMemberRefreshResponse> refreshAccessToken(
		@RequestBody final OauthMemberRefreshRequest request) {
		OauthMemberRefreshResponse response = memberService.refreshAccessToken(request, LocalDateTime.now());
		return ApiResponse.success(OauthSuccessCode.OK_REFRESH_TOKEN, response);
	}

	@PostMapping("/auth/signup")
	public ApiResponse<Void> signup(@RequestPart(required = false) MultipartFile profileImageFile,
		@RequestPart("signupData") SignUpRequest request) {
		memberService.signup(profileImageFile, request);
		return ApiResponse.success(MemberSuccessCode.OK_SIGNUP);
	}

	@PostMapping("/auth/signup/verifyEmail")
	public ApiResponse<Void> sendEmailVerif(@RequestBody final VerifyEmailRequest request) {
		memberService.sendEmailVerif(request);
		return ApiResponse.success(MemberSuccessCode.OK_SEND_EMAIL_VERIF);
	}

	@PostMapping("/auth/signup/verifyCode")
	public ApiResponse<Void> checkVerifCode(@RequestBody VerifCodeRequest request) {
		memberService.checkVerifCode(request);
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

	@PutMapping(value = "/profile", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ApiResponse<ProfileChangeResponse> changeProfile(
		@RequestPart(required = false) MultipartFile profileImageFile,
		@RequestPart(value = "profileInformation") ProfileChangeRequest request,
		@AuthPrincipalMember AuthMember authMember) {
		return ApiResponse.success(MemberSuccessCode.OK_MODIFIED_PROFILE,
			memberService.changeProfile(profileImageFile, authMember, request));
	}

	@PutMapping("/account/password")
	public ApiResponse<Void> changePassword(@RequestBody ModifyPasswordRequest request,
		@AuthPrincipalMember AuthMember authMember) {
		memberService.modifyPassword(request, authMember);
		return ApiResponse.success(MemberSuccessCode.OK_PASSWORD_CHANGED);
	}

	@DeleteMapping("/account")
	public ApiResponse<Void> deleteAccount(@RequestAttribute final String accessToken,
		@RequestBody final OauthMemberLogoutRequest request, @AuthPrincipalMember AuthMember authMember) {
		memberService.logout(accessToken, request);
		memberService.deleteMember(authMember);
		return ApiResponse.success(MemberSuccessCode.OK_DELETED_ACCOUNT);
	}
}
