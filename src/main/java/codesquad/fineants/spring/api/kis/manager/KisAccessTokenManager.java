package codesquad.fineants.spring.api.kis.manager;

import static codesquad.fineants.spring.api.kis.service.KisRedisService.*;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KisAccessTokenManager {
	private String accessToken;
	private String tokenType;
	private LocalDateTime expirationDatetime;

	public static KisAccessTokenManager from(Map<String, Object> accessTokenMap) {
		return new KisAccessTokenManager(
			(String)accessTokenMap.get("access_token"),
			(String)accessTokenMap.get("token_type"),
			LocalDateTime.parse((String)accessTokenMap.get("access_token_token_expired"),
				formatter));
	}

	public boolean isAccessTokenExpired(LocalDateTime dateTime) {
		if (expirationDatetime == null) {
			return true;
		}
		log.info("액세스 토큰 만료 체크, expirationDatetime : {}", expirationDatetime);
		return dateTime.isAfter(expirationDatetime);
	}

	public void refreshAccessToken(Map<String, Object> accessTokenMap) {
		this.accessToken = (String)accessTokenMap.get("access_token");
		this.tokenType = (String)accessTokenMap.get("token_type");
		this.expirationDatetime = LocalDateTime.parse((String)accessTokenMap.get("access_token_token_expired"),
			formatter);
	}

	public String createAuthorization() {
		return String.format("%s %s", tokenType, accessToken);
	}
}

