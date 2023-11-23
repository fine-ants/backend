package codesquad.fineants.spring.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import codesquad.fineants.domain.oauth.support.AuthPrincipalArgumentResolver;
import codesquad.fineants.spring.intercetpor.LogoutInterceptor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final AuthPrincipalArgumentResolver authPrincipalArgumentResolver;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new LogoutInterceptor())
			.excludePathPatterns("/api/*")
			.addPathPatterns("/api/auth/logout");
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(authPrincipalArgumentResolver);
		WebMvcConfigurer.super.addArgumentResolvers(resolvers);
	}
}
