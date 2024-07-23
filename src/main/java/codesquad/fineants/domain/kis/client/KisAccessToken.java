package codesquad.fineants.domain.kis.client;

import java.time.Duration;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KisAccessToken {
	@JsonProperty("access_token")
	private String accessToken;
	@JsonProperty("token_type")
	private String tokenType;
	@JsonProperty("access_token_token_expired")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime accessTokenExpired;
	@JsonProperty("expires_in")
	private Integer expiresIn;

	public KisAccessToken(String accessToken, String tokenType, LocalDateTime accessTokenExpired, Integer expiresIn) {
		this.accessToken = accessToken;
		this.tokenType = tokenType;
		this.accessTokenExpired = accessTokenExpired;
		this.expiresIn = expiresIn;
	}

	public static KisAccessToken bearerType(String accessToken, LocalDateTime accessTokenExpired, Integer expiresIn) {
		return new KisAccessToken(accessToken, "Bearer", accessTokenExpired, expiresIn);
	}

	public Duration betweenSecondFrom(LocalDateTime localDateTime) {
		return Duration.ofSeconds(Duration.between(localDateTime, accessTokenExpired).toSeconds());
	}

	public boolean isAccessTokenExpired(LocalDateTime dateTime) {
		return accessTokenExpired != null && dateTime.isAfter(accessTokenExpired);
	}

	public String createAuthorization() {
		return String.format("%s %s", tokenType, accessToken);
	}
}
