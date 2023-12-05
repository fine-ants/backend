package codesquad.fineants.spring.api.member.request;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OauthMemberLoginServiceRequest {
	private String provider;
	private String code;
	private String redirectUrl;
	private String state;
	private LocalDateTime requestTime;

	public static OauthMemberLoginServiceRequest of(String provider, String code, String redirectUrl, String state,
		LocalDateTime requestTime) {
		return new OauthMemberLoginServiceRequest(provider, code, redirectUrl, state, requestTime);
	}
}
