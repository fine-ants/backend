package co.fineants.api.domain.kis.domain.dto.response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Getter;

@Getter
@JsonDeserialize(using = KisHolidayWrapper.KisHolidayWrapperDeserializer.class)
public class KisHolidayWrapper {
	private final List<KisHoliday> holidays;

	private KisHolidayWrapper(List<KisHoliday> holidays) {
		this.holidays = holidays;
	}

	static class KisHolidayWrapperDeserializer extends JsonDeserializer<KisHolidayWrapper> {
		@Override
		public KisHolidayWrapper deserialize(JsonParser parser, DeserializationContext context) throws
			IOException {
			TreeNode rootNode = parser.readValueAsTree();
			TreeNode treeNode = rootNode.get("output");
			List<KisHoliday> holidays = new ArrayList<>();
			for (int i = 0; i < treeNode.size(); i++) {
				holidays.add(parser.getCodec().treeToValue(treeNode.get(i), KisHoliday.class));
			}
			return new KisHolidayWrapper(holidays);
		}
	}
}
