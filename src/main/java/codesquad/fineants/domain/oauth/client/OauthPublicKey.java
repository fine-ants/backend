package codesquad.fineants.domain.oauth.client;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class OauthPublicKey {
	private String kid; // 공개키 ID
	private String kty; // 공개키 타입, RSA로 고정
	private String alg; // 암호화 알고리즘
	private String use; // 공개키의 용도, sig(서명)으로 고정
	private String n; // 공개키 모듈(Modulus), 공개키는 n과 e의 쌍으로 구성됨
	private String e; // 공개키 지수(Exponent), 공개키는 n과 e의 쌍으로 구성됨

	public boolean equalKid(String kid) {
		return this.kid.equals(kid);
	}
}
