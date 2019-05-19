package info.matsumana.armeria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.spring.AnnotatedServiceRegistrationBean;

import info.matsumana.armeria.handler.RootHandler;

@Configuration
public class ArmeriaHttpServiceConfig {

    @Bean
    public AnnotatedServiceRegistrationBean rootHandlerRegistrationBean(RootHandler handler) {
        return new AnnotatedServiceRegistrationBean()
                .setServiceName("rootService")
                .setService(handler);
    }
}
