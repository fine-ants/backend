package codesquad.fineants.spring.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConditionalOnProperty(value = "scheduling.enabled", havingValue = "true", matchIfMissing = true)
@EnableScheduling
@Configuration
public class SchedulingConfig {
}
