package JJinBBang.app.global.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.logging.LogbackMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class MetricsConfig {

    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(Environment environment) {
        return registry -> registry.config().commonTags(
                "service", environment.getProperty("spring.application.name", "jjinbbang-be"),
                "environment", environment.getProperty("spring.profiles.active", "default")
        );
    }

    public TimedAspect timedAspect(MeterRegistry meterRegistry) {
        return new TimedAspect(meterRegistry);
    }

    public MeterBinder classLoaderMetrics() {
        return new ClassLoaderMetrics();
    }

    public MeterBinder jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    public MeterBinder jvmGcMetrics() {
        return new JvmGcMetrics();
    }

    public MeterBinder jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }

    public MeterBinder processorMetrics() {
        return new ProcessorMetrics();
    }

    public MeterBinder uptimeMetrics() {
        return new UptimeMetrics();
    }

    public MeterBinder logbackMetrics() {
        return new LogbackMetrics();
    }
}