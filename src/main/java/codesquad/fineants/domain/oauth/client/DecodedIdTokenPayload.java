package codesquad.fineants.domain.oauth.client;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import codesquad.fineants.spring.api.errors.errorcode.OauthErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DecodedIdTokenPayload {
	private String iss;
	private String aud;
	private String sub;
	private Integer iat;
	private Long exp;
	private String nonce;
	private Integer auth_time;
	private String picture;
	private String email;

	public void validateIdToken(String iss, String aud, LocalDateTime now, String nonce) {
		if (!this.iss.equals(iss)) {
			throw new BadRequestException(OauthErrorCode.WRONG_ID_TOKEN,
				"iss 값이 " + iss + "값과 일치하지 않습니다. iss=" + this.iss);
		}
		if (!this.aud.equals(aud)) {
			throw new BadRequestException(OauthErrorCode.WRONG_ID_TOKEN,
				"aud 값이 " + aud + "값과 일치하지 않습니다. aud=" + this.aud);
		}
		LocalDateTime expDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(exp), ZoneId.systemDefault());
		if (expDateTime.isBefore(now)) {
			throw new BadRequestException(OauthErrorCode.WRONG_ID_TOKEN,
				"exp값이 만료되었습니다. exp=" + expDateTime);
		}
		if (!this.nonce.equals(nonce)) {
			throw new BadRequestException(OauthErrorCode.WRONG_ID_TOKEN,
				"nonce 값이 " + nonce + "값과 일치하지 않습니다. nonce=" + this.nonce);
		}
	}
}
