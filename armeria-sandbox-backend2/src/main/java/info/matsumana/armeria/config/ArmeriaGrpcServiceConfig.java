package info.matsumana.armeria.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.common.grpc.GrpcSerializationFormats;
import com.linecorp.armeria.server.brave.BraveService;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.server.throttling.ThrottlingHttpService;
import com.linecorp.armeria.spring.GrpcExampleRequest;
import com.linecorp.armeria.spring.GrpcServiceRegistrationBean;

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
    public GrpcServiceRegistrationBean pingService(PingHandler handler) {
        return new GrpcServiceRegistrationBean()
                .setServiceName("pingService")
                .setService(GrpcService.builder()
                                       .addService(handler)
                                       // see https://line.github.io/armeria/server-docservice.html
                                       .supportedSerializationFormats(GrpcSerializationFormats.values())
                                       .enableUnframedRequests(true)
                                       .build())
                .setDecorators(BraveService.newDecorator(tracing),
                               LoggingService.newDecorator())
                .setExampleRequests(List.of(GrpcExampleRequest.of(PingServiceGrpc.SERVICE_NAME,
                                                                  "Ping",
                                                                  PingRequest.newBuilder().build())));
    }

    @Bean
    public GrpcServiceRegistrationBean hello2Service(Hello2Handler handler) {
        return new GrpcServiceRegistrationBean()
                .setServiceName("hello2Service")
                .setService(GrpcService.builder()
                                       .addService(handler)
                                       // see https://line.github.io/armeria/server-docservice.html
                                       .supportedSerializationFormats(GrpcSerializationFormats.values())
                                       .enableUnframedRequests(true)
                                       .build())
                .setDecorators(
                        BraveService.newDecorator(tracing),
                        ThrottlingHttpService.newDecorator(throttlingHelper.newThrottlingStrategy("backend2")),
                        LoggingService.newDecorator())
                .setExampleRequests(List.of(GrpcExampleRequest.of(Hello2ServiceGrpc.SERVICE_NAME,
                                                                  "Hello",
                                                                  Hello2Request.newBuilder()
                                                                               .setName("Armeria")
                                                                               .build())));
    }
}
