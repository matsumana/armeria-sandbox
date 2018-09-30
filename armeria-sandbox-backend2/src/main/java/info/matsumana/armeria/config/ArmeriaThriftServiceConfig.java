package info.matsumana.armeria.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.server.thrift.THttpService;
import com.linecorp.armeria.server.tracing.HttpTracingService;
import com.linecorp.armeria.spring.ThriftServiceRegistrationBean;

import brave.Tracing;
import info.matsumana.armeria.common.config.ZipkinTracingFactory;
import info.matsumana.armeria.thrift.Hello2Service;
import info.matsumana.armeria.thrift.PingService;

@Configuration
public class ArmeriaThriftServiceConfig {

    private final Tracing tracing;

    ArmeriaThriftServiceConfig(ZipkinTracingFactory tracingFactory) {
        tracing = tracingFactory.create("backend2");
    }

    @Bean
    public ThriftServiceRegistrationBean pingService(PingService.AsyncIface service) {
        return new ThriftServiceRegistrationBean()
                .setPath("/thrift/ping")
                .setService(THttpService.of(service)
                                        .decorate(LoggingService.newDecorator())
                                        .decorate(HttpTracingService.newDecorator(tracing)))
                .setServiceName("PingService")
                .setExampleRequests(List.of(new PingService.ping_args()));
    }

    @Bean
    public ThriftServiceRegistrationBean hello2Service(Hello2Service.AsyncIface service) {
        return new ThriftServiceRegistrationBean()
                .setPath("/thrift/hello2")
                .setService(THttpService.of(service)
                                        .decorate(LoggingService.newDecorator())
                                        .decorate(HttpTracingService.newDecorator(tracing)))
                .setServiceName("Hello2Service")
                .setExampleRequests(List.of(new Hello2Service.hello_args("foo")));
    }
}
