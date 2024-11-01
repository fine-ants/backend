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
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import co.fineants.FineAntsApplication;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1)
@Fork(1)
@Measurement(iterations = 10)
public class PortfolioCacheServiceTest {

	private ConfigurableApplicationContext context;
	private PortfolioCacheService service;

	@Setup
	public void init() {

		context = new SpringApplicationBuilder()
			.sources(FineAntsApplication.class)
			.web(WebApplicationType.NONE)
			.build()
			.run();
		context.registerShutdownHook();
		service = context.getBean(PortfolioCacheService.class);
	}

	@TearDown
	public void closeContext() {
		context.close();
	}

	@Benchmark
	public Set<String> getTickerSymbolsFromPortfolioBy() {
		Long portfolioId = 1L;
		return service.getTickerSymbolsFromPortfolioBy(portfolioId);
	}
}
