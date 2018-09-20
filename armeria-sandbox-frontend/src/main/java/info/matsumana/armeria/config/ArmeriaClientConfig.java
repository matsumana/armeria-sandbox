package info.matsumana.armeria.config;

import static com.linecorp.armeria.client.endpoint.EndpointSelectionStrategy.WEIGHTED_ROUND_ROBIN;
import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.client.Client;
import com.linecorp.armeria.client.ClientBuilder;
import com.linecorp.armeria.client.Endpoint;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerBuilder;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerRpcClient;
import com.linecorp.armeria.client.circuitbreaker.MetricCollectingCircuitBreakerListener;
import com.linecorp.armeria.client.endpoint.EndpointGroup;
import com.linecorp.armeria.client.endpoint.EndpointGroupRegistry;
import com.linecorp.armeria.client.endpoint.StaticEndpointGroup;
import com.linecorp.armeria.client.endpoint.healthcheck.HttpHealthCheckedEndpointGroup;
import com.linecorp.armeria.client.endpoint.healthcheck.HttpHealthCheckedEndpointGroupBuilder;
import com.linecorp.armeria.client.tracing.HttpTracingClient;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.RpcRequest;
import com.linecorp.armeria.common.RpcResponse;

import brave.Tracing;
import info.matsumana.armeria.thrift.Hello1Service;
import info.matsumana.armeria.thrift.Hello2Service;
import info.matsumana.armeria.thrift.Hello3Service;
import io.micrometer.core.instrument.MeterRegistry;

@Configuration
public class ArmeriaClientConfig {

    private final ApiServerSetting apiServerSetting;
    private final MeterRegistry meterRegistry;
    private final Tracing tracing;

    ArmeriaClientConfig(ApiServerSetting apiServerSetting, MeterRegistry meterRegistry,
                        ZipkinTracingFactory tracingFactory) {
        this.apiServerSetting = apiServerSetting;
        this.meterRegistry = meterRegistry;
        tracing = tracingFactory.create("frontend");
    }

    @Bean
    Hello1Service.AsyncIface hello1Service() {
        final EndpointGroup group =
                new StaticEndpointGroup(apiServerSetting.getBackend1().stream()
                                                        .map(setting -> Endpoint.of(setting.getHost(),
                                                                                    setting.getPort()))
                                                        .collect(toUnmodifiableList()));
        registerEndpointGroup(group, "backend1");
        return new ClientBuilder(String.format("tbinary+h2c://group:%s/thrift/hello1", "backend1"))
                .decorator(HttpRequest.class, HttpResponse.class,
                           HttpTracingClient.newDecorator(tracing, "backend1"))
                .decorator(RpcRequest.class, RpcResponse.class,
                           newCircuitBreakerDecorator("frontend-cb-1"))
                .build(Hello1Service.AsyncIface.class);
    }

    @Bean
    Hello2Service.AsyncIface hello2Service() {
        final EndpointGroup group =
                new StaticEndpointGroup(apiServerSetting.getBackend2().stream()
                                                        .map(setting -> Endpoint.of(setting.getHost(),
                                                                                    setting.getPort()))
                                                        .collect(toUnmodifiableList()));
        registerEndpointGroup(group, "backend2");
        return new ClientBuilder(String.format("tbinary+h2c://group:%s/thrift/hello2", "backend2"))
                .decorator(HttpRequest.class, HttpResponse.class,
                           HttpTracingClient.newDecorator(tracing, "backend2"))
                .decorator(RpcRequest.class, RpcResponse.class,
                           newCircuitBreakerDecorator("frontend-cb-2"))
                .build(Hello2Service.AsyncIface.class);
    }

    @Bean
    Hello3Service.AsyncIface hello3Service() {
        final EndpointGroup group =
                new StaticEndpointGroup(apiServerSetting.getBackend3().stream()
                                                        .map(setting -> Endpoint.of(setting.getHost(),
                                                                                    setting.getPort()))
                                                        .collect(toUnmodifiableList()));
        registerEndpointGroup(group, "backend3");
        return new ClientBuilder(String.format("tbinary+h2c://group:%s/thrift/hello3", "backend3"))
                .decorator(HttpRequest.class, HttpResponse.class,
                           HttpTracingClient.newDecorator(tracing, "backend3"))
                .decorator(RpcRequest.class, RpcResponse.class,
                           newCircuitBreakerDecorator("frontend-cb-3"))
                .build(Hello3Service.AsyncIface.class);
    }

    private void registerEndpointGroup(EndpointGroup group, String groupName) {
        final HttpHealthCheckedEndpointGroup healthCheckedGroup =
                new HttpHealthCheckedEndpointGroupBuilder(group, "/internal/healthcheck")
                        .build();
        if (EndpointGroupRegistry.register(groupName, healthCheckedGroup, WEIGHTED_ROUND_ROBIN)) {
            healthCheckedGroup.newMeterBinder(groupName).bindTo(meterRegistry);
        }
    }

    private Function<Client<RpcRequest, RpcResponse>, CircuitBreakerRpcClient> newCircuitBreakerDecorator(
            String circuitBreakerName) {
        return CircuitBreakerRpcClient.newPerHostDecorator(
//        return CircuitBreakerRpcClient.newPerHostAndMethodDecorator(
                key -> new CircuitBreakerBuilder(circuitBreakerName)
                        .listener(new MetricCollectingCircuitBreakerListener(meterRegistry))
//                        .failureRateThreshold(0.1)
                        .build(),
                response -> response.completionFuture()
                                    .handle((res, cause) -> cause == null));
    }
}
