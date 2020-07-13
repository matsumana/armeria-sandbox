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
import info.matsumana.armeria.thrift.Hello3Service;
import info.matsumana.armeria.thrift.PingService;

@Configuration
public class ArmeriaThriftServiceConfig {

    private final Tracing tracing;
    private final ThrottlingHelper throttlingHelper;

    ArmeriaThriftServiceConfig(ZipkinTracingFactory tracingFactory, ThrottlingHelper throttlingHelper) {
        tracing = tracingFactory.create("backend3");
        this.throttlingHelper = throttlingHelper;
    }

    @Bean
    public ArmeriaServerConfigurator armeriaServerConfigurator(PingService.AsyncIface pingService,
                                                               Hello3Service.AsyncIface hello3Service) {
        return serverBuilder -> serverBuilder
                .service("/thrift/ping",
                         THttpService.of(pingService)
                                     .decorate(BraveService.newDecorator(tracing))
                                     .decorate(LoggingService.newDecorator()))
                .service("/thrift/hello3",
                         THttpService.of(hello3Service)
                                     .decorate(BraveService.newDecorator(tracing))
                                     .decorate(ThrottlingService.newDecorator(
                                             throttlingHelper.newThrottlingStrategy("backend3")))
                                     .decorate(LoggingService.newDecorator()));
    }

    @Bean
    public DocServiceConfigurator docServiceConfigurator() {
        return docServiceBuilder -> {
            docServiceBuilder.exampleRequest(new PingService.ping_args())
                             .exampleRequest(new Hello3Service.hello_args("foo"));
        };
    }
}
