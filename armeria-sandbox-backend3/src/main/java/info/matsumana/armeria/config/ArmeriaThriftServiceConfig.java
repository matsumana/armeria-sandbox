package info.matsumana.armeria.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.server.brave.BraveService;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.server.thrift.THttpService;
import com.linecorp.armeria.server.throttling.ThrottlingHttpService;
import com.linecorp.armeria.spring.ThriftServiceRegistrationBean;

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
    public ThriftServiceRegistrationBean pingService(PingService.AsyncIface service) {
        return new ThriftServiceRegistrationBean()
                .setServiceName("pingService")
                .setPath("/thrift/ping")
                .setService(THttpService.of(service)
                                        .decorate(BraveService.newDecorator(tracing))
                                        .decorate(LoggingService.newDecorator()))
                .setExampleRequests(List.of(new PingService.ping_args()));
    }

    @Bean
    public ThriftServiceRegistrationBean hello3Service(Hello3Service.AsyncIface service) {
        return new ThriftServiceRegistrationBean()
                .setServiceName("hello3Service")
                .setPath("/thrift/hello3")
                .setService(THttpService.of(service)
                                        .decorate(ThrottlingHttpService.newDecorator(
                                                throttlingHelper.newThrottlingStrategy("backend3")))
                                        .decorate(BraveService.newDecorator(tracing))
                                        .decorate(LoggingService.newDecorator()))
                .setExampleRequests(List.of(new Hello3Service.hello_args("foo")));
    }
}
