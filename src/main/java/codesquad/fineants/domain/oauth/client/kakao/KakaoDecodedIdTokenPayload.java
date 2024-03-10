package codesquad.fineants.domain.oauth.client.kakao;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.fasterxml.jackson.annotation.JsonProperty;

import codesquad.fineants.domain.oauth.client.DecodedIdTokenPayload;
import codesquad.fineants.spring.api.common.errors.errorcode.OauthErrorCode;
import codesquad.fineants.spring.api.common.errors.exception.BadRequestException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KakaoDecodedIdTokenPayload implements DecodedIdTokenPayload {
	private String iss;
	private String aud;
	private String sub;
	private Integer iat;
	private Integer exp;
	private String nonce;
	@JsonProperty("auth_time")
	private Integer authTime;
	private String picture;
	private String email;

	@Override
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
				String.format("nonce 값 %s과 일치하지 않습니다. nonce=%s", this.nonce, nonce));
		}
	}

	@Override
	public String getPicture() {
		return picture;
	}

	@Override
	public String getEmail() {
		return email;
	}
}
