package info.matsumana.armeria.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;

import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.server.thrift.THttpService;
import com.linecorp.armeria.server.thrift.ThriftCallService;
import com.linecorp.armeria.server.tracing.HttpTracingService;
import com.linecorp.armeria.spring.ThriftServiceRegistrationBean;

import brave.Tracing;
import info.matsumana.armeria.thrift.HelloService;
import info.matsumana.armeria.thrift.PingService;

@Configuration
public class ArmeriaThriftConfig {

    private final Tracing tracing;

    ArmeriaThriftConfig(ZipkinTracingFactory tracingFactory,
                        @Value("${zipkin.service-name}") String tracingServiceName) {
        tracing = tracingFactory.create(tracingServiceName);
    }

    @Bean
    public ThriftServiceRegistrationBean pingService(PingService.Iface service) {
        return new ThriftServiceRegistrationBean()
                .setPath("/thrift/ping")
                .setService(ThriftCallService.of(service)
                                             .decorate(THttpService.newDecorator())
                                             .decorate(LoggingService.newDecorator())
                                             .decorate(HttpTracingService.newDecorator(tracing)))
                .setServiceName("PingService")
                .setExampleRequests(ImmutableList.of(new PingService.ping_args()));
    }

    @Bean
    public ThriftServiceRegistrationBean helloService(HelloService.Iface service) {
        return new ThriftServiceRegistrationBean()
                .setPath("/thrift/hello")
                .setService(ThriftCallService.of(service)
                                             .decorate(THttpService.newDecorator())
                                             .decorate(LoggingService.newDecorator())
                                             .decorate(HttpTracingService.newDecorator(tracing)))
                .setServiceName("HelloService")
                .setExampleRequests(ImmutableList.of(new HelloService.hello_args("foo")));
    }
}
