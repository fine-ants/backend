package co.fineants.api.domain.kis.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.ToString;

@ToString
public class KisSubscribeResponse {

	@JsonProperty("header")
	private Header header;
	@JsonProperty("body")
	private Body body;

	@ToString
	private static class Header {
		@JsonProperty("tr_id")
		private String trId;
		@JsonProperty("tr_key")
		private String trKey;
		@JsonProperty("encrypt")
		private String encrypt;
	}

	@ToString
	private static class Body {
		@JsonProperty("rt_cd")
		private String rtCd;

		@JsonProperty("msg_cd")
		private String msgCd;

		@JsonProperty("msg1")
		private String msg1;

		@JsonProperty("output")
		private Output output;
	}

	@ToString
	private static class Output {
		@JsonProperty("iv")
		private String iv;

		@JsonProperty("key")
		private String key;
	}
}
