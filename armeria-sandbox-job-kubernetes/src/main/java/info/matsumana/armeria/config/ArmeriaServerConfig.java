package info.matsumana.armeria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.spring.ArmeriaServerConfigurator;

import info.matsumana.armeria.handler.RootHandler;

@Configuration
public class ArmeriaServerConfig {

    @Bean
    public ArmeriaServerConfigurator armeriaServerConfigurator(RootHandler rootHandler) {
        return builder -> builder.annotatedService()
                                 .build(rootHandler);
    }
}
