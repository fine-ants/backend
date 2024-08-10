package codesquad.fineants.global.common.delay;

import java.time.Duration;

public interface DelayManager {

	Duration DEFAULT_DELAY = Duration.ofMillis(50);

	default Duration getDelay() {
		return DEFAULT_DELAY;
	}
}
