package codesquad.fineants.domain.kis.domain.dto.response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(using = KisDividendWrapper.KissDividendWrapperDeserializer.class)
public class KisDividendWrapper {
	private List<KisDividend> kisDividends;

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
