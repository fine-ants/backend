package codesquad.fineants.spring.api.stock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.oauth.support.AuthPrincipalArgumentResolver;
import codesquad.fineants.spring.api.errors.handler.GlobalExceptionHandler;
import codesquad.fineants.spring.config.JpaAuditingConfiguration;
import codesquad.fineants.spring.config.SpringConfig;

@ActiveProfiles("test")
@WebMvcTest(controllers = StockRestController.class)
@Import(value = {SpringConfig.class})
@MockBean(JpaAuditingConfiguration.class)
public class StockRestControllerTest {

	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AuthPrincipalArgumentResolver authPrincipalArgumentResolver;
}
