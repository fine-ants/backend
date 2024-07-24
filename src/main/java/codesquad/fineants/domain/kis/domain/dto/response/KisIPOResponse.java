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
@JsonDeserialize(using = KisIPOResponse.KisIPOResponseDeserializer.class)
public class KisIPOResponse {
	private List<KisIpo> datas;

	static class KisIPOResponseDeserializer extends JsonDeserializer<KisIPOResponse> {
		@Override
		public KisIPOResponse deserialize(JsonParser parser, DeserializationContext context) throws
			IOException {
			TreeNode rootNode = parser.readValueAsTree();
			TreeNode treeNode = rootNode.get("output1");
			KisIPOResponse response = new KisIPOResponse();
			List<KisIpo> data = new ArrayList<>();
			for (int i = 0; i < treeNode.size(); i++) {
				data.add(parser.getCodec().treeToValue(treeNode.get(i), KisIpo.class));
			}
			response.datas = data;
			return response;
		}
	}
}
