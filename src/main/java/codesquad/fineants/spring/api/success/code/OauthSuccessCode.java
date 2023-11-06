package codesquad.fineants.spring.api.success.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OauthSuccessCode implements SuccessCode {
	CREATED_SIGNUP(HttpStatus.CREATED, "회원가입에 성공하였습니다."),
	OK_LOGIN(HttpStatus.OK, "로그인에 성공하였습니다."),
	OK_LOGOUT(HttpStatus.OK, "로그아웃에 성공하였습니다."),
	OK_REFRESH_TOKEN(HttpStatus.OK, "액세스 토큰 갱신에 성공하였습니다.");

	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String toString() {
		return String.format("%s, %s(name=%s, httpStatus=%s, message=%s)", "Oauth 성공 코드",
			this.getClass().getSimpleName(),
			name(),
			httpStatus,
			message);
	}
}
