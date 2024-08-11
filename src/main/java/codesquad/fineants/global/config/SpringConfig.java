package codesquad.fineants.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import codesquad.fineants.domain.kis.properties.OauthKisProperties;
import codesquad.fineants.domain.portfolio.properties.PortfolioProperties;
import codesquad.fineants.global.init.AdminProperties;
import codesquad.fineants.global.init.ManagerProperties;
import codesquad.fineants.global.init.RoleProperties;
import codesquad.fineants.global.init.UserProperties;

@EnableAspectJAutoProxy
@EnableConfigurationProperties(value = {PortfolioProperties.class, AdminProperties.class, ManagerProperties.class,
	OauthKisProperties.class, RoleProperties.class, UserProperties.class})
@Configuration
public class SpringConfig {
}
