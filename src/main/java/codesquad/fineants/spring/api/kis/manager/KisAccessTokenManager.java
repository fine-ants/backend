package codesquad.fineants.spring.api.kis.manager;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KisAccessTokenManager {
	private String accessToken;
	private String tokenType;
	private LocalDateTime expirationDatetime;

	public static KisAccessTokenManager from(Map<String, Object> accessTokenMap) {
		return new KisAccessTokenManager(
			(String)accessTokenMap.get("access_token"),
			(String)accessTokenMap.get("token_type"),
			LocalDateTime.now().plusSeconds((int)accessTokenMap.get("expires_in"))
		);
	}

	public boolean isAccessTokenExpired(LocalDateTime dateTime) {
		if (expirationDatetime == null) {
			return true;
		}
		return dateTime.isAfter(expirationDatetime);
	}

	public void refreshAccessToken(Map<String, Object> accessTokenMap) {
		this.accessToken = (String)accessTokenMap.get("access_token");
		this.tokenType = (String)accessTokenMap.get("token_type");
		this.expirationDatetime = LocalDateTime.now().plusSeconds((int)accessTokenMap.get("expires_in"));
	}

	public String createAuthorization() {
		return String.format("%s %s", tokenType, accessToken);
	}
}

