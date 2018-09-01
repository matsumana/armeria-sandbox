package info.matsumana.armeria.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.spring.ArmeriaServerConfigurator;

@Configuration
public class ArmeriaConfig {

    @Bean
    ArmeriaServerConfigurator armeriaServerConfigurator() {
        final ExecutorService es = Executors.newFixedThreadPool(32);
        return sb -> sb.blockingTaskExecutor(es);
    }
}
