package codesquad.fineants.domain.kis.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.domain.kis.client.KisClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
class OauthKisClientTest {

	@Autowired
	private KisClient kisClient;
}
