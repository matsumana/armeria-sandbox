package info.matsumana.armeria.config;

import static com.linecorp.armeria.client.endpoint.EndpointSelectionStrategy.WEIGHTED_ROUND_ROBIN;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.client.Client;
import com.linecorp.armeria.client.ClientBuilder;
import com.linecorp.armeria.client.ClientRequestContext;
import com.linecorp.armeria.client.brave.BraveClient;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreaker;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerHttpClient;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerRpcClient;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerStrategy;
import com.linecorp.armeria.client.circuitbreaker.MetricCollectingCircuitBreakerListener;
import com.linecorp.armeria.client.endpoint.EndpointGroup;
import com.linecorp.armeria.client.endpoint.EndpointGroupRegistry;
import com.linecorp.armeria.client.endpoint.healthcheck.HealthCheckedEndpointGroup;
import com.linecorp.armeria.client.logging.LoggingClient;
import com.linecorp.armeria.client.retry.Backoff;
import com.linecorp.armeria.client.retry.RetryStrategy;
import com.linecorp.armeria.client.retry.RetryStrategyWithContent;
import com.linecorp.armeria.client.retry.RetryingHttpClient;
import com.linecorp.armeria.client.retry.RetryingRpcClient;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.RpcRequest;
import com.linecorp.armeria.common.RpcResponse;

import brave.Tracing;
import info.matsumana.armeria.grpc.Hello2ServiceGrpc.Hello2ServiceFutureStub;
import info.matsumana.armeria.helper.EndpointGroupHelper;
import info.matsumana.armeria.thrift.Hello1Service;
import info.matsumana.armeria.thrift.Hello3Service;
import io.micrometer.core.instrument.MeterRegistry;

@Configuration
public class ArmeriaClientConfig {

    private static final int MAX_TOTAL_ATTEMPTS = 3;

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
                .decorator(BraveClient.newDecorator(tracing, "backend1"))
                .rpcDecorator(RetryingRpcClient.newDecorator(newRetryStrategy(), MAX_TOTAL_ATTEMPTS))
                .rpcDecorator(newCircuitBreakerRpcDecorator())
                .decorator(LoggingClient.newDecorator())
                .build(Hello1Service.AsyncIface.class);
    }

    @Bean
    Hello2ServiceFutureStub hello2Service() {
        final EndpointGroup group = endpointGroupHelper.newEndpointGroup("/backend2.json",
                                                                         apiServerSetting.getBackend2());
        registerEndpointGroup(group, "backend2");
        return new ClientBuilder(String.format("gproto+h2c://group:%s/", "backend2"))
                .decorator(BraveClient.newDecorator(tracing, "backend2"))
                .decorator(RetryingHttpClient.newDecorator(RetryStrategy.onServerErrorStatus(),
                                                           MAX_TOTAL_ATTEMPTS))
                .decorator(newCircuitBreakerHttpDecorator())
                .decorator(LoggingClient.newDecorator())
                .build(Hello2ServiceFutureStub.class);
    }

    @Bean
    Hello3Service.AsyncIface hello3Service() {
        final EndpointGroup group = endpointGroupHelper.newEndpointGroup("/backend3.json",
                                                                         apiServerSetting.getBackend3());
        registerEndpointGroup(group, "backend3");
        return new ClientBuilder(String.format("tbinary+h2c://group:%s/thrift/hello3", "backend3"))
                .decorator(BraveClient.newDecorator(tracing, "backend3"))
                .rpcDecorator(RetryingRpcClient.newDecorator(newRetryStrategy(), MAX_TOTAL_ATTEMPTS))
                .rpcDecorator(newCircuitBreakerRpcDecorator())
                .decorator(LoggingClient.newDecorator())
                .build(Hello3Service.AsyncIface.class);
    }

    private void registerEndpointGroup(EndpointGroup group, String groupName) {
        final HealthCheckedEndpointGroup healthCheckedGroup = HealthCheckedEndpointGroup.of(group,
                                                                                            "/internal/healthcheck");
        if (EndpointGroupRegistry.register(groupName, healthCheckedGroup, WEIGHTED_ROUND_ROBIN)) {
            healthCheckedGroup.newMeterBinder(groupName).bindTo(meterRegistry);
        }
    }

    private Function<Client<RpcRequest, RpcResponse>, CircuitBreakerRpcClient> newCircuitBreakerRpcDecorator() {
        return CircuitBreakerRpcClient.newPerHostDecorator(
//        return CircuitBreakerRpcClient.newPerHostAndMethodDecorator(
                groupName -> CircuitBreaker.builder("frontend" + '_' + groupName)
                                           .listener(new MetricCollectingCircuitBreakerListener(meterRegistry))
                                           .failureRateThreshold(0.1)  // TODO need tuning
                                           .build(),
                (ctx, response) -> response.completionFuture()
                                           .handle((res, cause) -> cause == null));
    }

    private Function<Client<HttpRequest, HttpResponse>, CircuitBreakerHttpClient> newCircuitBreakerHttpDecorator() {
        return CircuitBreakerHttpClient.newPerHostDecorator(
//        return CircuitBreakerHttpClient.newPerHostAndMethodDecorator(
                groupName -> CircuitBreaker.builder("frontend" + '_' + groupName)
                                           .listener(new MetricCollectingCircuitBreakerListener(meterRegistry))
                                           .failureRateThreshold(0.1)  // TODO need tuning
                                           .build(),
                CircuitBreakerStrategy.onServerErrorStatus());
    }

    private static RetryStrategyWithContent<RpcResponse> newRetryStrategy() {
        return new RetryStrategyWithContent<>() {
            final Backoff backoff = Backoff.ofDefault();

            @Override
            public CompletionStage<Backoff> shouldRetry(ClientRequestContext ctx, RpcResponse response) {
                if (response.cause() == null) {
                    return CompletableFuture.completedFuture(null);
                } else {
                    return CompletableFuture.completedFuture(backoff);
                }
            }
        };
    }
}
