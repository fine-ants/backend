package codesquad.fineants.domain.oauth.client;

import java.time.LocalDateTime;

public interface DecodedIdTokenPayload {
	void validateIdToken(String iss, String aud, LocalDateTime now, String nonce);

	String getEmail();

	String getPicture();
}
