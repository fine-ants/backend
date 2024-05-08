package codesquad.fineants.domain.member.domain.dto.request;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OauthMemberLoginRequest {
	private String provider;
	private String code;
	private String redirectUrl;
	private String state;
	private LocalDateTime requestTime;

	public static OauthMemberLoginRequest of(String provider, String code, String redirectUrl, String state,
		LocalDateTime requestTime) {
		return new OauthMemberLoginRequest(provider, code, redirectUrl, state, requestTime);
	}

	public Map<String, String> toTokenBodyMap() {
		Map<String, String> result = new HashMap<>();
		result.put("code", code);
		result.put("redirectUrl", redirectUrl);
		result.put("state", state);
		return result;
	}
}
