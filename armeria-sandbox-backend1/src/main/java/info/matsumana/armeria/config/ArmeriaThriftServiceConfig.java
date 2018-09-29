package info.matsumana.armeria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;

import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.server.thrift.THttpService;
import com.linecorp.armeria.server.tracing.HttpTracingService;
import com.linecorp.armeria.spring.ThriftServiceRegistrationBean;

import brave.Tracing;
import info.matsumana.armeria.thrift.FailService;
import info.matsumana.armeria.thrift.Hello1Service;
import info.matsumana.armeria.thrift.PingService;

@Configuration
public class ArmeriaThriftServiceConfig {

    private final Tracing tracing;

    ArmeriaThriftServiceConfig(ZipkinTracingFactory tracingFactory) {
        tracing = tracingFactory.create("backend1");
    }

    @Bean
    public ThriftServiceRegistrationBean pingService(PingService.AsyncIface service) {
        return new ThriftServiceRegistrationBean()
                .setPath("/thrift/ping")
                .setService(THttpService.of(service)
                                        .decorate(LoggingService.newDecorator())
                                        .decorate(HttpTracingService.newDecorator(tracing)))
                .setServiceName("PingService")
                .setExampleRequests(ImmutableList.of(new PingService.ping_args()));
    }

    @Bean
    public ThriftServiceRegistrationBean failService(FailService.AsyncIface service) {
        return new ThriftServiceRegistrationBean()
                .setPath("/thrift/fail")
                .setService(THttpService.of(service)
                                        .decorate(LoggingService.newDecorator())
                                        .decorate(HttpTracingService.newDecorator(tracing)))
                .setServiceName("FailService")
                .setExampleRequests(ImmutableList.of(new FailService.fail_args()));
    }

    @Bean
    public ThriftServiceRegistrationBean hello1Service(Hello1Service.AsyncIface service) {
        return new ThriftServiceRegistrationBean()
                .setPath("/thrift/hello1")
                .setService(THttpService.of(service)
                                        .decorate(LoggingService.newDecorator())
                                        .decorate(HttpTracingService.newDecorator(tracing)))
                .setServiceName("Hello1Service")
                .setExampleRequests(ImmutableList.of(new Hello1Service.hello_args("foo")));
    }
}
