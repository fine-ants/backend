package codesquad.fineants.domain.portfolio.properties;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import lombok.Getter;

@Getter
@ConfigurationProperties(prefix = "portfolio")
public class PortfolioProperties {
	private final List<String> securitiesFirms;

	@ConstructorBinding
	public PortfolioProperties(String[] securitiesFirm) {
		this.securitiesFirms = Arrays.asList(securitiesFirm);
	}

	public boolean contains(String securitiesFirm) {
		return this.securitiesFirms.contains(securitiesFirm);
	}
}
