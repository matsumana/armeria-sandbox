package info.matsumana.armeria;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.linecorp.armeria.common.metric.PrometheusMeterRegistries;

import io.micrometer.prometheus.PrometheusMeterRegistry;

@SpringBootApplication
public class TestContext {

    // enable Prometheus metrics in test
    @Bean
    public PrometheusMeterRegistry newRegistry() {
        return PrometheusMeterRegistries.defaultRegistry();
    }
}
