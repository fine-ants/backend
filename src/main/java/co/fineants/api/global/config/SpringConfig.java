package co.fineants.api.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

import co.fineants.api.domain.kis.properties.KisProperties;
import co.fineants.api.domain.kis.properties.KisTrIdProperties;
import co.fineants.api.domain.portfolio.properties.PortfolioProperties;
import co.fineants.api.global.init.properties.AdminProperties;
import co.fineants.api.global.init.properties.ManagerProperties;
import co.fineants.api.global.init.properties.RoleProperties;
import co.fineants.api.global.init.properties.UserProperties;

@EnableAspectJAutoProxy
@EnableAsync
@EnableConfigurationProperties(value = {PortfolioProperties.class, AdminProperties.class, ManagerProperties.class,
	KisProperties.class, RoleProperties.class, UserProperties.class, KisTrIdProperties.class})
@EnableCaching
@Configuration
public class SpringConfig {
}
