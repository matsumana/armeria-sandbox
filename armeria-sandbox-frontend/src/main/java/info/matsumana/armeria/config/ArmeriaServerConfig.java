package info.matsumana.armeria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.server.brave.BraveService;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.server.throttling.ThrottlingService;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;
import com.linecorp.armeria.spring.DocServiceConfigurator;

import brave.Tracing;
import info.matsumana.armeria.handler.HelloHandler;
import info.matsumana.armeria.handler.RootHandler;
import info.matsumana.armeria.helper.ThrottlingHelper;

@Configuration
public class ArmeriaServerConfig {

    private final Tracing tracing;
    private final ThrottlingHelper throttlingHelper;

    ArmeriaServerConfig(ZipkinTracingFactory tracingFactory, ThrottlingHelper throttlingHelper) {
        tracing = tracingFactory.create("frontend");
        this.throttlingHelper = throttlingHelper;
    }

    @Bean
    public ArmeriaServerConfigurator armeriaServerConfigurator(RootHandler rootHandler,
                                                               HelloHandler helloHandler) {
        return builder -> builder
                // rootHandler
                .annotatedService()
                .decorator(BraveService.newDecorator(tracing))
                .decorator(LoggingService.newDecorator())
                .build(rootHandler)
                // helloHandler
                .annotatedService()
                .decorator(BraveService.newDecorator(tracing))
                .decorator(ThrottlingService.newDecorator(
                        throttlingHelper.newThrottlingStrategy("frontend")))
                .decorator(LoggingService.newDecorator())
                .build(helloHandler);
    }

    @Bean
    public DocServiceConfigurator docServiceConfigurator() {
        return builder -> builder.examplePaths(HelloHandler.class, "hello", "/hello/foo")
                                 .build();
    }
}
