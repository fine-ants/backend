package co.fineants.api.domain.kis.domain.dto.response;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.api.global.util.ObjectMapperUtil;

class KisSubscribeResponseTest {

	@DisplayName("json 응답을 역직렬화한다")
	@Test
	void test() {
		// given
		String json = "{\"header\":{\"tr_id\":\"H0STCNT0\",\"tr_key\":\"005930\",\"encrypt\":\"N\"},"
			+ "\"body\":{\"rt_cd\":\"0\",\"msg_cd\":\"OPSP0000\",\"msg1\":\"SUBSCRIBE SUCCESS\","
			+ "\"output\":{\"iv\":\"3fd42f1ab2e0234e\",\"key\":\"cxmarvcogvpurpvcucyehkzatftraqrc\"}}}";
		// when
		KisSubscribeResponse response = ObjectMapperUtil.deserialize(json, KisSubscribeResponse.class);
		// then
		Assertions.assertThat(response).isNotNull();
	}
}
