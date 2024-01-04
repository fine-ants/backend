package codesquad.fineants.spring.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

import codesquad.fineants.spring.api.portfolio.properties.PortfolioProperties;

@EnableScheduling
@EnableAspectJAutoProxy
@EnableConfigurationProperties(value = PortfolioProperties.class)
@Configuration
public class SpringConfig {
}
