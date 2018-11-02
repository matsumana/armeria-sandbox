package info.matsumana.armeria.config;

import static com.linecorp.armeria.client.endpoint.EndpointSelectionStrategy.WEIGHTED_ROUND_ROBIN;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.client.Client;
import com.linecorp.armeria.client.ClientBuilder;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerBuilder;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerRpcClient;
import com.linecorp.armeria.client.circuitbreaker.MetricCollectingCircuitBreakerListener;
import com.linecorp.armeria.client.endpoint.EndpointGroup;
import com.linecorp.armeria.client.endpoint.EndpointGroupRegistry;
import com.linecorp.armeria.client.endpoint.healthcheck.HttpHealthCheckedEndpointGroup;
import com.linecorp.armeria.client.endpoint.healthcheck.HttpHealthCheckedEndpointGroupBuilder;
import com.linecorp.armeria.client.logging.LoggingClient;
import com.linecorp.armeria.client.retry.Backoff;
import com.linecorp.armeria.client.retry.RetryStrategy;
import com.linecorp.armeria.client.retry.RetryingRpcClient;
import com.linecorp.armeria.client.tracing.HttpTracingClient;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.RpcRequest;
import com.linecorp.armeria.common.RpcResponse;

import brave.Tracing;
import info.matsumana.armeria.helper.EndpointGroupHelper;
import info.matsumana.armeria.thrift.Hello1Service;
import info.matsumana.armeria.thrift.Hello2Service;
import info.matsumana.armeria.thrift.Hello3Service;
import io.micrometer.core.instrument.MeterRegistry;

@Configuration
public class ArmeriaClientConfig {

    private final ApiServerSetting apiServerSetting;
    private final MeterRegistry meterRegistry;
    private final EndpointGroupHelper endpointGroupHelper;
    private final Tracing tracing;

    ArmeriaClientConfig(ApiServerSetting apiServerSetting, MeterRegistry meterRegistry,
                        ZipkinTracingFactory tracingFactory, EndpointGroupHelper endpointGroupHelper) {
        this.apiServerSetting = apiServerSetting;
        this.meterRegistry = meterRegistry;
        this.endpointGroupHelper = endpointGroupHelper;
        tracing = tracingFactory.create("frontend");
    }

    @Bean
    Hello1Service.AsyncIface hello1Service() {
        final EndpointGroup group = endpointGroupHelper.newEndpointGroup("/backend1.json",
                                                                         apiServerSetting.getBackend1());
        registerEndpointGroup(group, "backend1");
        return new ClientBuilder(String.format("tbinary+h2c://group:%s/thrift/hello1", "backend1"))
                .decorator(HttpRequest.class, HttpResponse.class,
                           HttpTracingClient.newDecorator(tracing, "backend1"))
                .decorator(RpcRequest.class, RpcResponse.class,
                           newCircuitBreakerDecorator())
                .decorator(RpcRequest.class, RpcResponse.class,
                           RetryingRpcClient.newDecorator(newRetryStrategy()))
                .decorator(HttpRequest.class, HttpResponse.class,
                           LoggingClient.newDecorator())
                .build(Hello1Service.AsyncIface.class);
    }

    @Bean
    Hello2Service.AsyncIface hello2Service() {
        final EndpointGroup group = endpointGroupHelper.newEndpointGroup("/backend2.json",
                                                                         apiServerSetting.getBackend2());
        registerEndpointGroup(group, "backend2");
        return new ClientBuilder(String.format("tbinary+h2c://group:%s/thrift/hello2", "backend2"))
                .decorator(HttpRequest.class, HttpResponse.class,
                           HttpTracingClient.newDecorator(tracing, "backend2"))
                .decorator(RpcRequest.class, RpcResponse.class,
                           newCircuitBreakerDecorator())
                .decorator(RpcRequest.class, RpcResponse.class,
                           RetryingRpcClient.newDecorator(newRetryStrategy()))
                .decorator(HttpRequest.class, HttpResponse.class,
                           LoggingClient.newDecorator())
                .build(Hello2Service.AsyncIface.class);
    }

    @Bean
    Hello3Service.AsyncIface hello3Service() {
        final EndpointGroup group = endpointGroupHelper.newEndpointGroup("/backend3.json",
                                                                         apiServerSetting.getBackend3());
        registerEndpointGroup(group, "backend3");
        return new ClientBuilder(String.format("tbinary+h2c://group:%s/thrift/hello3", "backend3"))
                .decorator(HttpRequest.class, HttpResponse.class,
                           HttpTracingClient.newDecorator(tracing, "backend3"))
                .decorator(RpcRequest.class, RpcResponse.class,
                           newCircuitBreakerDecorator())
                .decorator(RpcRequest.class, RpcResponse.class,
                           RetryingRpcClient.newDecorator(newRetryStrategy()))
                .decorator(HttpRequest.class, HttpResponse.class,
                           LoggingClient.newDecorator())
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

    private Function<Client<RpcRequest, RpcResponse>, CircuitBreakerRpcClient> newCircuitBreakerDecorator() {
        return CircuitBreakerRpcClient.newPerHostDecorator(
//        return CircuitBreakerRpcClient.newPerHostAndMethodDecorator(
                groupName -> new CircuitBreakerBuilder("frontend" + '_' + groupName)
                        .listener(new MetricCollectingCircuitBreakerListener(meterRegistry))
                        .failureRateThreshold(0.1)  // TODO need tuning
                        .build(),
                response -> response.completionFuture()
                                    .handle((res, cause) -> cause == null));
    }

    private static RetryStrategy<RpcRequest, RpcResponse> newRetryStrategy() {
        return new RetryStrategy<>() {
            final Backoff backoff = RetryStrategy.defaultBackoff;

            @Override
            public CompletionStage<Backoff> shouldRetry(RpcRequest request, RpcResponse response) {
                if (response.cause() == null) {
                    return CompletableFuture.completedFuture(null);
                } else {
                    return CompletableFuture.completedFuture(backoff);
                }
            }
        };
    }
}
