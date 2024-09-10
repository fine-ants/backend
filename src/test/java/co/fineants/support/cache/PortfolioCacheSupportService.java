package co.fineants.support.cache;

import java.util.Collections;
import java.util.Set;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioCacheSupportService {
	private static final String CACHE_NAME = "tickerSymbols";
	private final CacheManager cacheManager;

	public Cache fetchCache() {
		return cacheManager.getCache(CACHE_NAME);
	}

	@SuppressWarnings(value = "unchecked")
	public Set<String> fetchTickers(Long portfolioId) {
		Cache cache = cacheManager.getCache(CACHE_NAME);
		if (cache != null) {
			Cache.ValueWrapper wrapper = cache.get(portfolioId);
			if (wrapper != null) {
				return (Set<String>)wrapper.get();
			}
		}
		return Collections.emptySet();
	}

	public void clear() {
		Cache cache = cacheManager.getCache(CACHE_NAME);
		if (cache != null) {
			cache.clear();
		}
	}
}
