package codesquad.fineants.global.success;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberSuccessCode implements SuccessCode {
	OK_MODIFIED_PROFILE(HttpStatus.OK, "프로필이 수정되었습니다"),
	OK_READ_PROFILE(HttpStatus.OK, "프로필 정보 조회에 성공하였습니다"),
	OK_MEMBER_TOWNS(HttpStatus.OK, "회원 동네 목록 조회를 완료하였습니다."),
	OK_SIGNUP(HttpStatus.CREATED, "회원가입이 완료되었습니다"),
	OK_SEND_VERIFY_CODE(HttpStatus.OK, "이메일로 검증 코드를 전송하였습니다"),
	OK_NICKNAME_CHECK(HttpStatus.OK, "닉네임이 사용가능합니다"),
	OK_EMAIL_CHECK(HttpStatus.OK, "이메일이 사용가능합니다"),
	OK_PASSWORD_CHANGED(HttpStatus.OK, "비밀번호를 성공적으로 변경했습니다"),
	OK_VERIF_CODE(HttpStatus.OK, "일치하는 인증번호 입니다"),
	OK_DELETED_ACCOUNT(HttpStatus.OK, "계정이 삭제되었습니다"),
	OK_LOGIN(HttpStatus.OK, "로그인에 성공하였습니다."),
	OK_READ_NOTIFICATIONS(HttpStatus.OK, "현재 알림 목록 조회를 성공했습니다"),
	OK_FETCH_ALL_NOTIFICATIONS(HttpStatus.OK, "알림을 모두 읽음 처리했습니다"),
	OK_DELETED_NOTIFICATION(HttpStatus.OK, "알림 삭제를 성공하였습니다"),
	OK_DELETED_ALL_NOTIFICATIONS(HttpStatus.OK, "알림 전체 삭제를 성공하였습니다"),
	OK_UPDATE_NOTIFICATION_PREFERENCE(HttpStatus.OK, "알림 설정을 변경했습니다"),
	OK_SEND_NOTIFICATION(HttpStatus.OK, "알림 메시지 생성 및 전송이 완료되었습니다");

	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String toString() {
		return String.format("%s, %s(name=%s, httpStatus=%s, message=%s)", "회원 성공 코드",
			this.getClass().getSimpleName(),
			name(),
			httpStatus,
			message);
	}
}
