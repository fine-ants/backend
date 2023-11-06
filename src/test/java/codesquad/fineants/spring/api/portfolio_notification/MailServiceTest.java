package codesquad.fineants.spring.api.portfolio_notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class MailServiceTest {

	@Autowired
	private MailService service;

	@DisplayName("서버는 이메일을 전송한다")
	@Test
	void sendEmail() {
		// given
		String to = "dragonbead95@naver.com";
		String subject = "스프링부트 메일 테스트";
		String body = "스프링부트 메일 테스트 내용입니다.";
		// when
		service.sendEmail(to, subject, body);
		// then

	}
}
