package info.matsumana.armeria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.server.brave.BraveService;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.server.thrift.THttpService;
import com.linecorp.armeria.server.throttling.ThrottlingService;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;
import com.linecorp.armeria.spring.DocServiceConfigurator;

import brave.Tracing;
import info.matsumana.armeria.helper.ThrottlingHelper;
import info.matsumana.armeria.thrift.Hello1Service;
import info.matsumana.armeria.thrift.PingService;

@Configuration
public class ArmeriaServerConfig {

    private final Tracing tracing;
    private final ThrottlingHelper throttlingHelper;

    ArmeriaServerConfig(ZipkinTracingFactory tracingFactory, ThrottlingHelper throttlingHelper) {
        tracing = tracingFactory.create("backend1");
        this.throttlingHelper = throttlingHelper;
    }

    @Bean
    public ArmeriaServerConfigurator armeriaServerConfigurator(PingService.AsyncIface pingService,
                                                               Hello1Service.AsyncIface hello1Service) {
        return builder -> builder
                .service("/thrift/ping",
                         THttpService.of(pingService)
                                     .decorate(BraveService.newDecorator(tracing))
                                     .decorate(LoggingService.newDecorator()))
                .service("/thrift/hello1",
                         THttpService.of(hello1Service)
                                     .decorate(BraveService.newDecorator(tracing))
                                     .decorate(ThrottlingService.newDecorator(
                                             throttlingHelper.newThrottlingStrategy("backend1")))
                                     .decorate(LoggingService.newDecorator()));
    }

    @Bean
    public DocServiceConfigurator docServiceConfigurator() {
        return builder -> builder
                .exampleRequests(PingService.class, "ping", new PingService.ping_args())
                .exampleRequests(Hello1Service.class, "hello", new Hello1Service.hello_args("foo"));
    }
}
