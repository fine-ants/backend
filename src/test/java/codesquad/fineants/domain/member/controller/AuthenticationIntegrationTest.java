package codesquad.fineants.domain.member.controller;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.global.util.ObjectMapperUtil;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

public class AuthenticationIntegrationTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberRepository memberRepository;

	@LocalServerPort
	private int port;

	@BeforeEach
	void setUp() {
		RestAssured.port = port;
	}

	@SuppressWarnings("checkstyle:NoWhitespaceBefore")
	@DisplayName("사용자는 일반 로그인한다")
	@Test
	void login() {
		// given
		memberRepository.save(createMember());

		Map<String, String> body = Map.of(
			"email", "dragonbead95@naver.com",
			"password", "nemo1234@"
		);
		String json = ObjectMapperUtil.serialize(body);
		// when & then
		given().log().all()
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.body(json)
			.when()
			.post("/api/auth/login")
			.then()
			.cookie("accessToken", notNullValue())
			.cookie("refreshToken", notNullValue())
			.log()
			.body()
			.statusCode(200)
			.assertThat()
			.body("data", Matchers.nullValue());
	}

	@DisplayName("사용자는 이메일 또는 비밀번호를 틀려서 로그인 할 수 없다")
	@Test
	void login_whenInvalidUsernameAndPassword_then401() {
		// given
		memberRepository.save(createMember());

		Map<String, String> body = Map.of(
			"email", "aaa",
			"password", "aaa"
		);
		String json = ObjectMapperUtil.serialize(body);
		// when & then
		given().log().all()
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.body(json)
			.when()
			.post("/api/auth/login")
			.then()
			.log()
			.body()
			.statusCode(401);
	}

	@DisplayName("사용자는 로그아웃한다")
	@Test
	void logout() {
		// given
		memberRepository.save(createMember());
		Map<String, String> cookies = processLogin();

		String url = "/api/auth/logout";
		// when
		given().log().all()
			.cookies(cookies)
			.when()
			.get(url)
			.then()
			.log()
			.body()
			.statusCode(200);

		// then
		given().log().all()
			.cookies(cookies)
			.when()
			.get("/api/profile")
			.then()
			.log()
			.body()
			.statusCode(401);
	}

	// TODO: 테스트 해결
	@Disabled
	@DisplayName("사용자는 프로필 조회를 요청하다가 액세스 토큰이 만료되어 갱신된다")
	@Test
	void refreshAccessToken() {
		// given
		memberRepository.save(createMember());
		Map<String, String> cookies = processLogin();

		// when & then
		given().log().all()
			.cookies(cookies)
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.when()
			.get("/api/profile")
			.then()
			.cookie("accessToken", notNullValue())
			.cookie("refreshToken", notNullValue())
			.log()
			.body()
			.statusCode(200);
	}

	private Map<String, String> processLogin() {
		Map<String, String> body = Map.of(
			"email", "dragonbead95@naver.com",
			"password", "nemo1234@"
		);
		String json = ObjectMapperUtil.serialize(body);
		ExtractableResponse<Response> extract = given()
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.body(json)
			.when()
			.post("/api/auth/login")
			.then()
			.log()
			.body()
			.statusCode(200)
			.extract();
		return extract.cookies();
	}
}
