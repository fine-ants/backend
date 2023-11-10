package codesquad.fineants.domain.oauth.client;

import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OauthPublicKeyList {
	private List<OauthPublicKey> keys;
}
