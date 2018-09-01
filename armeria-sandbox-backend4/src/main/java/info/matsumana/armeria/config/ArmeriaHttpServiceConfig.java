package info.matsumana.armeria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.server.tracing.HttpTracingService;
import com.linecorp.armeria.spring.AnnotatedServiceRegistrationBean;

import brave.Tracing;
import info.matsumana.armeria.controller.HelloController;
import info.matsumana.armeria.controller.RootController;

@Configuration
public class ArmeriaHttpServiceConfig {

    private final Tracing tracing;

    ArmeriaHttpServiceConfig(ZipkinTracingFactory tracingFactory) {
        tracing = tracingFactory.create("backend4");
    }

    @Bean
    public AnnotatedServiceRegistrationBean rootControllerRegistrationBean(RootController controller) {
        return new AnnotatedServiceRegistrationBean()
                .setServiceName("rootController")
                .setService(controller)
                .setDecorators(LoggingService.newDecorator(),
                               HttpTracingService.newDecorator(tracing));
    }

    @Bean
    public AnnotatedServiceRegistrationBean helloControllerRegistrationBean(HelloController controller) {
        return new AnnotatedServiceRegistrationBean()
                .setServiceName("helloController")
                .setService(controller)
                .setDecorators(LoggingService.newDecorator(),
                               HttpTracingService.newDecorator(tracing));
    }
}
