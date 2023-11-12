package codesquad.fineants.domain.jwt;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Jwt {

	private final String accessToken;
	private final String refreshToken;
	@JsonIgnore
	private Date expireDateAccessToken;
	@JsonIgnore
	private Date expireDateRefreshToken;

	public Jwt(String accessToken, String refreshToken, Date expireDateAccessToken, Date expireDateRefreshToken) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.expireDateAccessToken = expireDateAccessToken;
		this.expireDateRefreshToken = expireDateRefreshToken;
	}

	public long convertExpireDateRefreshTokenTimeWithLong() {
		return expireDateRefreshToken.getTime();
	}
}
