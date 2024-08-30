package co.fineants.api.domain.kis.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KisHeader {
	AUTHORIZATION("authorization"),
	APP_KEY("appkey"),
	APP_SECRET("appsecret"),
	TR_ID("tr_id"),
	CONTENT_TYPE("content-type"),
	ACCEPT("accept"),
	CUSTOMER_TYPE("custtype");

	private final String headerName;

}
