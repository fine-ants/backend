package codesquad.fineants.domain.member.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.success.MemberSuccessCode;

@RestController
public class OauthRestController {

	@GetMapping("/api/oauth/redirect")
	public ApiResponse<Map<String, String>> oauthRedirect(
		@RequestParam String accessToken,
		@RequestParam String refreshToken) {
		Map<String, String> result = new HashMap<>();
		result.put("accessToken", accessToken);
		result.put("refreshToken", refreshToken);
		return ApiResponse.success(MemberSuccessCode.OK_LOGIN, result);
	}
}
