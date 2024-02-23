package codesquad.fineants.spring.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import codesquad.fineants.spring.api.portfolio.properties.PortfolioProperties;

@EnableAspectJAutoProxy
@EnableConfigurationProperties(value = PortfolioProperties.class)
@Configuration
public class SpringConfig {
}
