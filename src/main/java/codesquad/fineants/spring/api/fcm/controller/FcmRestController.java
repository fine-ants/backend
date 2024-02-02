package codesquad.fineants.spring.api.fcm.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalMember;
import codesquad.fineants.spring.api.fcm.request.FcmRegisterRequest;
import codesquad.fineants.spring.api.fcm.response.FcmRegisterResponse;
import codesquad.fineants.spring.api.fcm.service.FcmService;
import codesquad.fineants.spring.api.response.ApiResponse;
import codesquad.fineants.spring.api.success.code.FcmSuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fcm")
public class FcmRestController {
	private final FcmService fcmService;

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/tokens")
	public ApiResponse<FcmRegisterResponse> registerToken(
		@Valid @RequestBody FcmRegisterRequest request,
		@AuthPrincipalMember AuthMember authMember) {
		return ApiResponse.success(FcmSuccessCode.CREATED_FCM, fcmService.registerToken(request, authMember));
	}
}
