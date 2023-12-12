package codesquad.fineants.spring.api.member.decoder;

import codesquad.fineants.domain.oauth.client.DecodedIdTokenPayload;
import codesquad.fineants.domain.oauth.client.google.GoogleDecodedIdTokenPayload;
import codesquad.fineants.spring.util.ObjectMapperUtil;

public class GoogleIDTokenDecoder extends IDTokenDecoder {
	@Override
	protected DecodedIdTokenPayload deserializeDecodedPayload(String payload) {
		return ObjectMapperUtil.deserialize(payload, GoogleDecodedIdTokenPayload.class);
	}
}
