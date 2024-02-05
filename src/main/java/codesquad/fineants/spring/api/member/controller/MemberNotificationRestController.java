package codesquad.fineants.spring.api.member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.spring.api.member.response.MemberNotificationResponse;
import codesquad.fineants.spring.api.member.service.MemberNotificationService;
import codesquad.fineants.spring.api.response.ApiResponse;
import codesquad.fineants.spring.api.success.code.MemberSuccessCode;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members/{memberId}")
@RequiredArgsConstructor
public class MemberNotificationRestController {

	private final MemberNotificationService notificationService;

	@GetMapping("/notifications")
	public ApiResponse<MemberNotificationResponse> readNotifications(@PathVariable Long memberId) {
		return ApiResponse.success(MemberSuccessCode.OK_READ_NOTIFICATIONS,
			notificationService.readNotifications(memberId));
	}
}
