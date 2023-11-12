package codesquad.fineants.spring.api.member.controller;

import static org.springframework.http.HttpStatus.*;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.spring.api.member.request.OauthMemberLogoutRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberRefreshRequest;
import codesquad.fineants.spring.api.member.response.OauthCreateUrlResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberLoginResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberRefreshResponse;
import codesquad.fineants.spring.api.member.service.MemberService;
import codesquad.fineants.spring.api.response.ApiResponse;
import codesquad.fineants.spring.api.success.code.OauthSuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/api/auth")
@RestController
public class MemberRestController {

	private final MemberService memberService;

	@PostMapping("/{provider}/url")
	public ApiResponse<OauthCreateUrlResponse> authorizationCodeURL(@PathVariable String provider) {
		return ApiResponse.success(OauthSuccessCode.OK_URL, memberService.createAuthorizationCodeURL(provider));
	}

	@PostMapping(value = "/{provider}/login")
	public ApiResponse<OauthMemberLoginResponse> login(
		@PathVariable String provider,
		@RequestParam String code,
		@RequestParam String redirectUrl,
		@RequestParam String state) {
		return ApiResponse.success(OauthSuccessCode.OK_LOGIN,
			memberService.login(provider, code, redirectUrl, state, LocalDateTime.now()));
	}

	@PostMapping(value = "/logout")
	public ApiResponse<Void> logout(@RequestAttribute String accessToken,
		@RequestBody OauthMemberLogoutRequest request) {
		memberService.logout(accessToken, request);
		return ApiResponse.success(OauthSuccessCode.OK_LOGOUT);
	}

	@ResponseStatus(OK)
	@PostMapping("/refresh/token")
	public ApiResponse<OauthMemberRefreshResponse> refreshAccessToken(@RequestBody OauthMemberRefreshRequest request) {
		OauthMemberRefreshResponse response = memberService.refreshAccessToken(request,
			LocalDateTime.now());
		return ApiResponse.success(OauthSuccessCode.OK_REFRESH_TOKEN, response);
	}

}
