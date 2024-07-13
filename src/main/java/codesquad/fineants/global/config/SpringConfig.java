package codesquad.fineants.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.querydsl.jpa.impl.JPAQueryFactory;

import codesquad.fineants.domain.portfolio.properties.PortfolioProperties;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@EnableAspectJAutoProxy
@EnableConfigurationProperties(value = PortfolioProperties.class)
@Configuration
public class SpringConfig {

	@PersistenceContext
	private EntityManager entityManager;

	@Bean
	public JPAQueryFactory jpaQueryFactory() {
		return new JPAQueryFactory(entityManager);
	}
}
