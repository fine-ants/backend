package codesquad.fineants.global.common.delay;

import java.time.Duration;

public interface DelayManager {

	Duration DEFAULT_DELAY = Duration.ofMillis(50);
	Duration TIMEOUT = Duration.ofMinutes(10);
	Duration FIXED_DELAY = Duration.ofSeconds(5);

	default Duration delay() {
		return DEFAULT_DELAY;
	}

	default Duration timeout() {
		return TIMEOUT;
	}

	default Duration fixedDelay() {
		return FIXED_DELAY;
	}
}
