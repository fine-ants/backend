package codesquad.fineants.spring.api.kis.response;

import java.util.List;

import lombok.ToString;

@ToString
public class KisRealTimeSigningPriceResponse {
	private String mkscShrnIscd; // 유가증권 단축 종목코드
	private String stckCntgHour; // 주식 체결 시간
	private String stckPrpr; // 주식 현재가

	public KisRealTimeSigningPriceResponse(List<String> stockInfos) {
		this.mkscShrnIscd = stockInfos.get(0);
		this.stckCntgHour = stockInfos.get(1);
		this.stckPrpr = stockInfos.get(2);
	}

}
