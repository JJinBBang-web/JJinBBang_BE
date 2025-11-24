package JJinBBang.app.domain.building.config;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class VWorldConfig {
	@Value("${vworld.geocode.core-pool-size:4}")
	private int geocodeCorePoolSize;
	@Value("${vworld.geocode.max-pool-size:4}")
	private int geocodeMaxPoolSize;
	@Value("${vworld.geocode.queue-capacity:100}")
	private int geocodeQueueCapacity;

	@Bean
	public Executor geocodeExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(geocodeCorePoolSize);   // 1 vCPU 기준 시작은 4개 정도면 적당
		executor.setMaxPoolSize(geocodeMaxPoolSize);
		executor.setQueueCapacity(geocodeQueueCapacity);
		executor.setThreadNamePrefix("geocode-");
		executor.initialize();
		return executor;
	}
}
