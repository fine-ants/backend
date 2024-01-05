package codesquad.fineants.spring.api.kis.manager;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import codesquad.fineants.spring.api.kis.client.KisAccessToken;
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
public class KisAccessTokenManager {

	private KisAccessToken accessToken;

	public boolean isAccessTokenExpired(LocalDateTime dateTime) {
		return accessToken.isAccessTokenExpired(dateTime);
	}

	public void refreshAccessToken(KisAccessToken accessToken) {
		this.accessToken = accessToken;
	}

	public String createAuthorization() {
		return accessToken.createAuthorization();
	}
}

