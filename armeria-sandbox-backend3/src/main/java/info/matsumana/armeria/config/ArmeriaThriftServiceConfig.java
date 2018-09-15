package info.matsumana.armeria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;

import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.server.thrift.THttpService;
import com.linecorp.armeria.server.thrift.ThriftCallService;
import com.linecorp.armeria.server.tracing.HttpTracingService;
import com.linecorp.armeria.spring.ThriftServiceRegistrationBean;

import brave.Tracing;
import info.matsumana.armeria.thrift.Hello3Service;
import info.matsumana.armeria.thrift.PingService;

@Configuration
public class ArmeriaThriftServiceConfig {

    private final Tracing tracing;

    ArmeriaThriftServiceConfig(ZipkinTracingFactory tracingFactory) {
        tracing = tracingFactory.create("backend3");
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
    public ThriftServiceRegistrationBean helloService(Hello3Service.Iface service) {
        return new ThriftServiceRegistrationBean()
                .setPath("/thrift/hello3")
                .setService(ThriftCallService.of(service)
                                             .decorate(THttpService.newDecorator())
                                             .decorate(LoggingService.newDecorator())
                                             .decorate(HttpTracingService.newDecorator(tracing)))
                .setServiceName("Hello3Service")
                .setExampleRequests(ImmutableList.of(new Hello3Service.hello_args("foo")));
    }
}
