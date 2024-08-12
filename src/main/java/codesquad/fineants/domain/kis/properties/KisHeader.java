package codesquad.fineants.domain.kis.properties;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum KisHeader {
	AUTHORIZATION("authorization"),
	APP_KEY("appkey"),
	APP_SECRET("appsecret"),
	TR_ID("tr_id");

	private final String headerName;
}
