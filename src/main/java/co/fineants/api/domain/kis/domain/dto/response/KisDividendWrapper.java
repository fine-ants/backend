package co.fineants.api.domain.kis.domain.dto.response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(using = KisDividendWrapper.KissDividendWrapperDeserializer.class)
// TODO: KisDividend 클래스의 역직렬화를 수정하여 래퍼 클래스가 없도록 수정
public class KisDividendWrapper {
	private List<KisDividend> kisDividends;

	public static KisDividendWrapper empty() {
		return new KisDividendWrapper(Collections.emptyList());
	}

	public static KisDividendWrapper create(List<KisDividend> kisDividends) {
		return new KisDividendWrapper(kisDividends);
	}

	static class KissDividendWrapperDeserializer extends JsonDeserializer<KisDividendWrapper> {
		@Override
		public KisDividendWrapper deserialize(JsonParser parser, DeserializationContext context) throws
			IOException {
			TreeNode rootNode = parser.readValueAsTree();
			TreeNode treeNode = rootNode.get("output1");
			KisDividendWrapper wrapper = new KisDividendWrapper();
			List<KisDividend> kisDividends = new ArrayList<>();
			for (int i = 0; i < treeNode.size(); i++) {
				kisDividends.add(parser.getCodec().treeToValue(treeNode.get(i), KisDividend.class));
			}
			wrapper.kisDividends = kisDividends;
			return wrapper;
		}
	}
}
