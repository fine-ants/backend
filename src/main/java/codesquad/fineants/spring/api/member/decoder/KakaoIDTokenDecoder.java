package codesquad.fineants.spring.api.member.decoder;

import codesquad.fineants.domain.oauth.client.DecodedIdTokenPayload;
import codesquad.fineants.domain.oauth.client.kakao.KakaoDecodedIdTokenPayload;
import codesquad.fineants.spring.util.ObjectMapperUtil;

public class KakaoIDTokenDecoder extends IDTokenDecoder {
	@Override
	protected DecodedIdTokenPayload deserializeDecodedPayload(String payload) {
		return ObjectMapperUtil.deserialize(payload, KakaoDecodedIdTokenPayload.class);
	}
}
