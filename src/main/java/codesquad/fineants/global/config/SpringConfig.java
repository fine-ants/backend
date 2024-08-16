package codesquad.fineants.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

import codesquad.fineants.domain.kis.properties.KisProperties;
import codesquad.fineants.domain.kis.properties.KisTrIdProperties;
import codesquad.fineants.domain.portfolio.properties.PortfolioProperties;
import codesquad.fineants.global.init.properties.AdminProperties;
import codesquad.fineants.global.init.properties.ManagerProperties;
import codesquad.fineants.global.init.properties.RoleProperties;
import codesquad.fineants.global.init.properties.UserProperties;

@EnableAspectJAutoProxy
@EnableAsync
@EnableConfigurationProperties(value = {PortfolioProperties.class, AdminProperties.class, ManagerProperties.class,
	KisProperties.class, RoleProperties.class, UserProperties.class, KisTrIdProperties.class})
@Configuration
public class SpringConfig {
}
