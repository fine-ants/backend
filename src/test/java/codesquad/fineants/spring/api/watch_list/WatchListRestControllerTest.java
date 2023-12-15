package codesquad.fineants.spring.api.watch_list;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalArgumentResolver;
import codesquad.fineants.spring.api.errors.handler.GlobalExceptionHandler;
import codesquad.fineants.spring.api.watch_list.request.CreateWatchListRequest;
import codesquad.fineants.spring.api.watch_list.request.CreateWatchStockRequest;
import codesquad.fineants.spring.api.watch_list.response.CreateWatchListResponse;
import codesquad.fineants.spring.config.JpaAuditingConfiguration;
import codesquad.fineants.spring.config.SpringConfig;

@ActiveProfiles("test")
@WebMvcTest(controllers = WatchListRestController.class)
@Import(value = {SpringConfig.class})
@MockBean(JpaAuditingConfiguration.class)
class WatchListRestControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private WatchListRestController watchListRestController;

	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AuthPrincipalArgumentResolver authPrincipalArgumentResolver;

	@MockBean
	private WatchListService watchListService;


	@BeforeEach
	void setUp(){
		mockMvc = MockMvcBuilders.standaloneSetup(watchListRestController)
			.setControllerAdvice(globalExceptionHandler)
			.setCustomArgumentResolvers(authPrincipalArgumentResolver)
			.alwaysDo(print())
			.build();
	}

	@DisplayName("사용자가 watchlist를 추가한다.")
	@Test
	void createWatchList() throws Exception {
		// given
		Member member = Member.builder()
			.id(1L)
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.provider("local")
			.password("kim1234@")
			.profileUrl("profileValue")
			.build();
		AuthMember authMember = AuthMember.from(member);

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("name", "My watchlist");
		String body = objectMapper.writeValueAsString(requestBodyMap);

		CreateWatchListResponse response = new CreateWatchListResponse(1L);

		given(authPrincipalArgumentResolver.supportsParameter(any())).willReturn(true);
		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(authMember);
		given(watchListService.createWatchList(any(AuthMember.class), any(CreateWatchListRequest.class)))
			.willReturn(response);

		// when & then
		mockMvc.perform(post("/api/watchlists")
			.contentType(MediaType.APPLICATION_JSON)
			.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심종목 목록이 추가되었습니다")))
			.andExpect(jsonPath("data.watchlistId").value(1));
	}

	@DisplayName("사용자가 watchlist에 종목을 추가한다.")
	@Test
	void createWatchStock() throws Exception {
		// given
		Member member = Member.builder()
			.id(1L)
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.provider("local")
			.password("kim1234@")
			.profileUrl("profileValue")
			.build();
		AuthMember authMember = AuthMember.from(member);

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("tickerSymbol", "005930");
		String body = objectMapper.writeValueAsString(requestBodyMap);

		given(authPrincipalArgumentResolver.supportsParameter(any())).willReturn(true);
		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(authMember);
		doNothing().when(watchListService).createWatchStock(any(AuthMember.class), any(Long.class), any(CreateWatchStockRequest.class));

		// when & then
		mockMvc.perform(post("/api/watchlists/1/stock")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심종목 목록에 종목이 추가되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자가 watchlist를 삭제한다.")
	@Test
	void deleteWatchList() throws Exception {
		// given
		Member member = Member.builder()
			.id(1L)
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.provider("local")
			.password("kim1234@")
			.profileUrl("profileValue")
			.build();
		AuthMember authMember = AuthMember.from(member);

		given(authPrincipalArgumentResolver.supportsParameter(any())).willReturn(true);
		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(authMember);
		doNothing().when(watchListService).deleteWatchList(any(AuthMember.class), any(Long.class));

		// when & then
		mockMvc.perform(delete("/api/watchlists/1")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심종목 목록이 삭제가 완료되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자가 watchlist에서 종목을 삭제한다.")
	@Test
	void deleteWatchStock() throws Exception {
		// given
		Member member = Member.builder()
			.id(1L)
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.provider("local")
			.password("kim1234@")
			.profileUrl("profileValue")
			.build();
		AuthMember authMember = AuthMember.from(member);

		given(authPrincipalArgumentResolver.supportsParameter(any())).willReturn(true);
		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(authMember);
		doNothing().when(watchListService).deleteWatchStock(any(AuthMember.class), any(Long.class), any(Long.class));

		// when & then
		mockMvc.perform(delete("/api/watchlists/1/stock/1")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("관심목록 종목이 삭제되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}
}
