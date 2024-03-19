package codesquad.fineants.spring.docs.watch_list;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.spring.api.watch_list.controller.WatchListRestController;
import codesquad.fineants.spring.api.watch_list.request.CreateWatchListRequest;
import codesquad.fineants.spring.api.watch_list.response.CreateWatchListResponse;
import codesquad.fineants.spring.api.watch_list.response.ReadWatchListsResponse;
import codesquad.fineants.spring.api.watch_list.service.WatchListService;
import codesquad.fineants.spring.docs.RestDocsSupport;
import codesquad.fineants.spring.util.ObjectMapperUtil;

public class WatchListRestControllerDocsTest extends RestDocsSupport {

	private final WatchListService service = Mockito.mock(WatchListService.class);

	@Override
	protected Object initController() {
		return new WatchListRestController(service);
	}

	@DisplayName("Watchlist 추가 API")
	@Test
	void createWatchList() throws Exception {
		// given
		given(service.createWatchList(
			ArgumentMatchers.any(AuthMember.class),
			ArgumentMatchers.any(CreateWatchListRequest.class)))
			.willReturn(CreateWatchListResponse.create(1L));

		Map<String, Object> body = Map.of(
			"name", "My watchlist"
		);

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/watchlists")
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심종목 목록이 추가되었습니다")))
			.andExpect(jsonPath("data.watchlistId").value(equalTo(1)))
			.andDo(
				document(
					"watchlist-create",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					requestFields(
						fieldWithPath("name").type(JsonFieldType.STRING)
							.description("관심 종목 목록 이름")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.OBJECT)
							.description("응답 데이터"),
						fieldWithPath("data.watchlistId").type(JsonFieldType.NUMBER)
							.description("관심 종목 리스트 등록 번호")
					)
				)
			);

	}

	@DisplayName("Watchlist 목록 조회 API")
	@Test
	void readWatchLists() throws Exception {
		// given
		Member member = createMember();

		given(service.readWatchLists(ArgumentMatchers.any(AuthMember.class)))
			.willReturn(ReadWatchListsResponse.from(List.of(
				createWatchList(member)
			)));

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.get("/api/watchlists")
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심목록 목록 조회가 완료되었습니다")))
			.andExpect(jsonPath("data[0].id").value(equalTo(1)))
			.andExpect(jsonPath("data[0].name").value(equalTo("my watchlist 1")))
			.andDo(
				document(
					"watchlist-multiple-search",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.ARRAY)
							.description("응답 데이터"),
						fieldWithPath("data[].id").type(JsonFieldType.NUMBER)
							.description("관심 종목 리스트 등록 번호"),
						fieldWithPath("data[].name").type(JsonFieldType.STRING)
							.description("관심 종목 리스트 이름")
					)
				)
			);

	}

}
