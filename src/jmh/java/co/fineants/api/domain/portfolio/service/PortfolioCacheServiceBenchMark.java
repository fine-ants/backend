package co.fineants.api.domain.portfolio.service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import co.fineants.FineAntsApplication;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
@Warmup(iterations = 1)
public class PortfolioCacheServiceBenchMark {

	private ConfigurableApplicationContext context;
	private PortfolioCacheService service;

	@Setup
	public void init() {
		context = SpringApplication.run(FineAntsApplication.class);
		context.registerShutdownHook();
		service = context.getBean(PortfolioCacheService.class);
	}

	@TearDown
	public void closeContext() {
		context.close();
	}

	@Benchmark
	@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.MINUTES)
	public Set<String> getTickerSymbolsFromPortfolioBy() {
		Long portfolioId = 1L;
		return service.getTickerSymbolsFromPortfolioBy(portfolioId);
	}
}
