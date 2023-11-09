package codesquad.fineants.domain.oauth.client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DecodedIdTokenHeader {
	private String kid;
	private String typ;
	private String alg;
}
