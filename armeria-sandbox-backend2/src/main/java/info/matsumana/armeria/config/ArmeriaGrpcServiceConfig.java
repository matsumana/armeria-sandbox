package info.matsumana.armeria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.common.grpc.GrpcSerializationFormats;
import com.linecorp.armeria.server.brave.BraveService;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.server.throttling.ThrottlingService;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;
import com.linecorp.armeria.spring.DocServiceConfigurator;

import brave.Tracing;
import info.matsumana.armeria.grpc.Hello2.Hello2Request;
import info.matsumana.armeria.grpc.Hello2ServiceGrpc;
import info.matsumana.armeria.grpc.Ping.PingRequest;
import info.matsumana.armeria.grpc.PingServiceGrpc;
import info.matsumana.armeria.handler.Hello2Handler;
import info.matsumana.armeria.handler.PingHandler;
import info.matsumana.armeria.helper.ThrottlingHelper;

@Configuration
public class ArmeriaGrpcServiceConfig {

    private final Tracing tracing;
    private final ThrottlingHelper throttlingHelper;

    ArmeriaGrpcServiceConfig(ZipkinTracingFactory tracingFactory, ThrottlingHelper throttlingHelper) {
        tracing = tracingFactory.create("backend2");
        this.throttlingHelper = throttlingHelper;
    }

    @Bean
    public ArmeriaServerConfigurator armeriaServerConfigurator(PingHandler pingHandler,
                                                               Hello2Handler hello2Handler) {
        final var pingService = GrpcService.builder()
                                           .addService(pingHandler)
                                           .supportedSerializationFormats(GrpcSerializationFormats.values())
                                           .enableUnframedRequests(true)
                                           .build();
        final var hello2Service = GrpcService.builder()
                                             .addService(hello2Handler)
                                             .supportedSerializationFormats(GrpcSerializationFormats.values())
                                             .enableUnframedRequests(true)
                                             .build();
        return serverBuilder -> serverBuilder
                .service(pingService,
                         BraveService.newDecorator(tracing),
                         LoggingService.newDecorator())
                .service(hello2Service,
                         BraveService.newDecorator(tracing),
                         ThrottlingService.newDecorator(
                                 throttlingHelper.newThrottlingStrategy("backend2")),
                         LoggingService.newDecorator());
    }

    @Bean
    public DocServiceConfigurator docServiceConfigurator() {
        return docServiceBuilder -> docServiceBuilder.exampleRequestForMethod(PingServiceGrpc.SERVICE_NAME,
                                                                              "Ping",
                                                                              PingRequest.newBuilder().build())
                                                     .exampleRequestForMethod(Hello2ServiceGrpc.SERVICE_NAME,
                                                                              "Hello",
                                                                              Hello2Request.newBuilder()
                                                                                           .setName("Armeria")
                                                                                           .build());
    }
}
