package codesquad.fineants.domain.kis.repository;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import codesquad.fineants.domain.kis.client.KisAccessToken;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class KisAccessTokenRepository {

	private KisAccessToken accessToken;

	public boolean isAccessTokenExpired(LocalDateTime dateTime) {
		if (accessToken == null) {
			return true;
		}
		return accessToken.isAccessTokenExpired(dateTime);
	}

	public void refreshAccessToken(KisAccessToken accessToken) {
		this.accessToken = accessToken;
	}

	public String createAuthorization() {
		return accessToken.createAuthorization();
	}

	public boolean isTokenExpiringSoon(LocalDateTime localDateTime) {
		if (accessToken == null) {
			return true;
		}
		return accessToken.betweenSecondFrom(localDateTime).toSeconds() < 3600;
	}
}

