package co.fineants.api.domain.kis.domain.dto.response;

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.global.util.ObjectMapperUtil;

class KisSearchStockInfoTest extends AbstractContainerBaseTest {

	@DisplayName("종목 정보를 역직렬화한다")
	@Test
	void givenJson_whenDeserialize_thenReturnInstance() {
		// given
		Map<String, Object> body = new HashMap<>();
		Map<String, Object> output = new HashMap<>();
		output.put("std_pdno", "KR7000660001");
		output.put("pdno", "00000A000660");
		output.put("prdt_name", "에스케이하이닉스보통주");
		output.put("prdt_eng_name", "SK hynix");
		output.put("mket_id_cd", "STK");
		output.put("idx_bztp_scls_cd_name", "전기,전자");
		output.put("lstg_abol_dt", "");
		body.put("output", output);

		String json = ObjectMapperUtil.serialize(body);
		// when
		KisSearchStockInfo actual = ObjectMapperUtil.deserialize(json, KisSearchStockInfo.class);
		// then
		KisSearchStockInfo expected = KisSearchStockInfo.listedStock("KR7000660001", "000660", "에스케이하이닉스보통주",
			"SK hynix", "STK", "전기,전자");
		Assertions.assertThat(actual).isEqualTo(expected);
	}

}
