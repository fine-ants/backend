package co.fineants.api.global.init;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Profile(value = {"local", "dev", "release", "production"})
@Component
@RequiredArgsConstructor
public class SetupDataLoaderListener implements ApplicationListener<ContextRefreshedEvent> {

	private boolean alreadySetup = false;
	private final SetupDataLoader setupDataLoader;

	@Transactional
	@Override
	public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
		if (alreadySetup) {
			return;
		}
		setupDataLoader.setupResources();
		alreadySetup = true;
	}
}
