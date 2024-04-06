package codesquad.fineants.spring;

import javax.validation.ConstraintValidator;

import org.springframework.web.bind.support.SpringWebConstraintValidatorFactory;
import org.springframework.web.context.WebApplicationContext;

public class TestConstrainValidationFactory extends SpringWebConstraintValidatorFactory {

	private final WebApplicationContext ctx;

	private boolean isValid = false;

	public TestConstrainValidationFactory(WebApplicationContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
		ConstraintValidator instance = super.getInstance(key);
		return (T)instance;
	}

	@Override
	protected WebApplicationContext getWebApplicationContext() {
		return ctx;
	}
}
