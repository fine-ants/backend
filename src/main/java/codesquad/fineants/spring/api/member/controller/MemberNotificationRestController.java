package codesquad.fineants.spring.api.member.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.spring.api.member.request.MemberNotificationAllDeleteRequest;
import codesquad.fineants.spring.api.member.request.MemberNotificationAllReadRequest;
import codesquad.fineants.spring.api.member.response.MemberNotificationResponse;
import codesquad.fineants.spring.api.member.service.MemberNotificationService;
import codesquad.fineants.spring.api.response.ApiResponse;
import codesquad.fineants.spring.api.success.code.MemberSuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/members/{memberId}")
@RequiredArgsConstructor
public class MemberNotificationRestController {

	private final MemberNotificationService notificationService;

	// 회원의 알림 목록 조회
	@GetMapping("/notifications")
	public ApiResponse<MemberNotificationResponse> fetchNotifications(@PathVariable Long memberId) {
		return ApiResponse.success(MemberSuccessCode.OK_READ_NOTIFICATIONS,
			notificationService.fetchNotifications(memberId));
	}

	// 회원의 알림 모두 읽기
	@PatchMapping("/notifications")
	public ApiResponse<Void> readAllNotifications(
		@PathVariable Long memberId,
		@Valid @RequestBody MemberNotificationAllReadRequest request) {
		List<Long> notificationIds = notificationService.readAllNotifications(memberId, request.getNotificationIds());
		log.info("회원 알림 모두 읽기 처리 결과 : memberId={}, 읽은 알림 등록 번호={}", memberId, notificationIds);
		return ApiResponse.success(MemberSuccessCode.OK_FETCH_ALL_NOTIFICATIONS);
	}

	// 회원의 알림 특정 읽기
	@PatchMapping("/notifications/{notificationId}")
	public ApiResponse<Void> readNotification(
		@PathVariable Long memberId,
		@PathVariable Long notificationId) {
		List<Long> notificationIds = notificationService.readAllNotifications(memberId, List.of(notificationId));
		log.info("회원 알림 모두 읽기 처리 결과 : memberId={}, 읽은 알림 등록 번호={}", memberId, notificationIds);
		return ApiResponse.success(MemberSuccessCode.OK_FETCH_ALL_NOTIFICATIONS);
	}

	@DeleteMapping("/notifications")
	public ApiResponse<Void> deleteAllNotifications(
		@PathVariable Long memberId,
		@Valid @RequestBody MemberNotificationAllDeleteRequest request) {
		List<Long> deletedNotificationIds = notificationService.deleteAllNotifications(
			memberId,
			request.getNotificationIds()
		);
		log.info("회원 알림 모두 삭제 처리 결과 : memberId={}, 삭제한 알림 등록 번호={}", memberId, deletedNotificationIds);
		return ApiResponse.success(MemberSuccessCode.OK_DELETED_ALL_NOTIFICATIONS);
	}

	@DeleteMapping("/notifications/{notificationId}")
	public ApiResponse<Void> deleteNotification(
		@PathVariable Long memberId,
		@PathVariable Long notificationId) {
		List<Long> deletedNotificationIds = notificationService.deleteAllNotifications(
			memberId,
			List.of(notificationId)
		);
		log.info("회원 알림 모두 삭제 처리 결과 : memberId={}, 삭제한 알림 등록 번호={}", memberId, deletedNotificationIds);
		return ApiResponse.success(MemberSuccessCode.OK_DELETD_NOTIFICATION);
	}
}
