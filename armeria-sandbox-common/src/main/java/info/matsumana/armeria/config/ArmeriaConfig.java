package info.matsumana.armeria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.common.metric.PrometheusMeterRegistries;

import io.micrometer.prometheus.PrometheusMeterRegistry;

@Configuration
public class ArmeriaConfig {

    @Bean
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        // Use BetterPrometheusNamingConvention
        return PrometheusMeterRegistries.newRegistry();
    }
}
