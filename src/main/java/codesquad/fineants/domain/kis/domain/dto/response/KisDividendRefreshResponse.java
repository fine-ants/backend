package codesquad.fineants.domain.kis.domain.dto.response;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class KisDividendRefreshResponse {
	private List<DividendItem> addedDividend;
	private List<DividendItem> delDividend;

	public static KisDividendRefreshResponse of(List<DividendItem> addedDividend, List<DividendItem> delDividend) {
		return new KisDividendRefreshResponse(new ArrayList<>(addedDividend), new ArrayList<>(delDividend));
	}
}
