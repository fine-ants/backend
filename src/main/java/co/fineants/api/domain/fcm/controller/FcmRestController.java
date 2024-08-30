package co.fineants.api.domain.fcm.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.fineants.api.domain.fcm.domain.dto.request.FcmRegisterRequest;
import co.fineants.api.domain.fcm.domain.dto.response.FcmDeleteResponse;
import co.fineants.api.domain.fcm.domain.dto.response.FcmRegisterResponse;
import co.fineants.api.domain.fcm.service.FcmService;
import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.api.global.security.oauth.resolver.MemberAuthenticationPrincipal;
import co.fineants.api.global.success.FcmSuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fcm/tokens")
public class FcmRestController {
	private final FcmService fcmService;

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	public ApiResponse<FcmRegisterResponse> createToken(
		@Valid @RequestBody FcmRegisterRequest request,
		@MemberAuthenticationPrincipal MemberAuthentication authentication) {
		FcmRegisterResponse response = fcmService.createToken(request, authentication.getId());
		log.info("FCM 토큰 등록 결과 : response={}", response);
		return ApiResponse.success(FcmSuccessCode.CREATED_FCM, response);
	}

	@DeleteMapping("/{fcmTokenId}")
	public ApiResponse<Void> deleteToken(@PathVariable Long fcmTokenId) {
		FcmDeleteResponse response = fcmService.deleteToken(fcmTokenId);
		log.info("FCM 토큰 삭제 결과 : response={}", response);
		return ApiResponse.success(FcmSuccessCode.OK_DELETE_FCM);
	}
}
