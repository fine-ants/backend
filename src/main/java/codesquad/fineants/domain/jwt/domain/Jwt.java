package codesquad.fineants.domain.jwt.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
@ToString
public class Jwt {

	private String accessToken;
	private String refreshToken;
	@JsonIgnore
	private Date expireDateAccessToken;
	@JsonIgnore
	private Date expireDateRefreshToken;
}
