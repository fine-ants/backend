package codesquad.fineants.domain.member.controller;

import static org.springframework.http.HttpStatus.*;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.member.domain.dto.request.OauthMemberRefreshRequest;
import codesquad.fineants.domain.member.domain.dto.response.OauthMemberRefreshResponse;
import codesquad.fineants.domain.member.service.MemberService;
import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.success.OauthSuccessCode;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthenticationRestController {

	private final MemberService memberService;

	@ResponseStatus(OK)
	@PostMapping("/auth/refresh/token")
	@PermitAll
	public ApiResponse<OauthMemberRefreshResponse> refreshAccessToken(
		@RequestBody final OauthMemberRefreshRequest request
	) {
		OauthMemberRefreshResponse response = memberService.refreshAccessToken(request, LocalDateTime.now());
		return ApiResponse.success(OauthSuccessCode.OK_REFRESH_TOKEN, response);
	}
}
