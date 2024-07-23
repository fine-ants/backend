package codesquad.fineants.domain.exchangerate.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import codesquad.fineants.ControllerTestSupport;
import codesquad.fineants.domain.exchangerate.service.ExchangeRateService;

@WebMvcTest(controllers = ExchangeRateRestController.class)
class ExchangeRateRestControllerTest extends ControllerTestSupport {

	@MockBean
	private ExchangeRateService exchangeRateService;

	@Override
	protected Object initController() {
		return new ExchangeRateRestController(exchangeRateService);
	}
}
