package codesquad.fineants.domain.kis.domain.dto.response;

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

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(using = KisIpoResponse.KisIpoResponseDeserializer.class)
public class KisIpoResponse {
	private List<KisIpo> kisIpos;

	public static KisIpoResponse empty() {
		return new KisIpoResponse(Collections.emptyList());
	}

	static class KisIpoResponseDeserializer extends JsonDeserializer<KisIpoResponse> {
		@Override
		public KisIpoResponse deserialize(JsonParser parser, DeserializationContext context) throws
			IOException {
			TreeNode rootNode = parser.readValueAsTree();
			TreeNode treeNode = rootNode.get("output1");
			KisIpoResponse response = new KisIpoResponse();
			List<KisIpo> data = new ArrayList<>();
			for (int i = 0; i < treeNode.size(); i++) {
				data.add(parser.getCodec().treeToValue(treeNode.get(i), KisIpo.class));
			}
			response.kisIpos = data;
			return response;
		}
	}
}
