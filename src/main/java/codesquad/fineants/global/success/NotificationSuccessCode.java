package codesquad.fineants.global.success;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public enum NotificationSuccessCode implements SuccessCode {
	CREATED_NOTIFICATION(HttpStatus.CREATED, "알림이 생성되었습니다"),
	OK_NOTIFY_PORTFOLIO_TARGET_GAIN_MESSAGES(HttpStatus.OK, "포트폴리오 목표 수익률 알림 메시지가 전송되었습니다"),
	OK_NOTIFY_PORTFOLIO_MAX_LOSS_MESSAGES(HttpStatus.OK, "포트폴리오 최대 손실율 알림 메시지가 전송되었습니다");

	private final HttpStatus httpStatus;
	private final String message;
}
