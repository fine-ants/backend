package co.fineants.api.global.common.delay;

import java.time.Duration;

public interface DelayManager {

	Duration DEFAULT_DELAY = Duration.ofMillis(50);
	Duration REQUEST_TIMEOUT = Duration.ofMinutes(10);
	Duration FIXED_DELAY = Duration.ofSeconds(5);
	Duration FIXED_ACCESS_TOKEN_DELAY = Duration.ofMinutes(1);

	default Duration delay() {
		return DEFAULT_DELAY;
	}

	default Duration timeout() {
		return REQUEST_TIMEOUT;
	}

	default Duration fixedDelay() {
		return FIXED_DELAY;
	}

	default Duration fixedAccessTokenDelay() {
		return FIXED_ACCESS_TOKEN_DELAY;
	}
}
