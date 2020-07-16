package info.matsumana.armeria.config;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.client.Clients;
import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.client.RpcClient;
import com.linecorp.armeria.client.brave.BraveClient;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreaker;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerClient;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerRpcClient;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerRule;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerRuleWithContent;
import com.linecorp.armeria.client.circuitbreaker.MetricCollectingCircuitBreakerListener;
import com.linecorp.armeria.client.endpoint.EndpointGroup;
import com.linecorp.armeria.client.endpoint.healthcheck.HealthCheckedEndpointGroup;
import com.linecorp.armeria.client.logging.LoggingClient;
import com.linecorp.armeria.client.retry.Backoff;
import com.linecorp.armeria.client.retry.RetryDecision;
import com.linecorp.armeria.client.retry.RetryRule;
import com.linecorp.armeria.client.retry.RetryRuleWithContent;
import com.linecorp.armeria.client.retry.RetryingClient;
import com.linecorp.armeria.client.retry.RetryingRpcClient;
import com.linecorp.armeria.common.HttpStatus;
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
        return Clients.builder("tbinary+h2c", group, "/thrift/hello1")
                      .decorator(BraveClient.newDecorator(tracing, "backend1"))
                      .rpcDecorator(RetryingRpcClient.newDecorator(newRetryRpcStrategy(), MAX_TOTAL_ATTEMPTS))
                      .rpcDecorator(newCircuitBreakerRpcDecorator())
                      .decorator(LoggingClient.newDecorator())
                      .build(Hello1Service.AsyncIface.class);
    }

    @Bean
    Hello2ServiceFutureStub hello2Service() {
        final EndpointGroup group = endpointGroupHelper.newEndpointGroup("/backend2.json",
                                                                         apiServerSetting.getBackend2());
        registerEndpointGroup(group, "backend2");
        return Clients.builder("gproto+h2c", group, "/")
                      .decorator(BraveClient.newDecorator(tracing, "backend2"))
                      .decorator(RetryingClient.newDecorator(
                              // Armeria's ThrottlingService returns 429 while throttling
                              RetryRule.of(RetryRule.onException(),
                                           RetryRule.onServerErrorStatus(),
                                           RetryRule.onStatus(HttpStatus.TOO_MANY_REQUESTS)),
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
        return Clients.builder("tbinary+h2c", group, "/thrift/hello3")
                      .decorator(BraveClient.newDecorator(tracing, "backend3"))
                      .rpcDecorator(RetryingRpcClient.newDecorator(newRetryRpcStrategy(), MAX_TOTAL_ATTEMPTS))
                      .rpcDecorator(newCircuitBreakerRpcDecorator())
                      .decorator(LoggingClient.newDecorator())
                      .build(Hello3Service.AsyncIface.class);
    }

    private void registerEndpointGroup(EndpointGroup group, String groupName) {
        final HealthCheckedEndpointGroup healthCheckedGroup = HealthCheckedEndpointGroup.of(group,
                                                                                            "/internal/healthcheck");
        healthCheckedGroup.newMeterBinder(groupName).bindTo(meterRegistry);
    }

    private Function<? super RpcClient, CircuitBreakerRpcClient> newCircuitBreakerRpcDecorator() {
        return CircuitBreakerRpcClient.newPerHostDecorator(
//        return CircuitBreakerRpcClient.newPerHostAndMethodDecorator(
                groupName -> CircuitBreaker.builder("frontend" + '_' + groupName)
                                           .listener(new MetricCollectingCircuitBreakerListener(meterRegistry))
                                           .failureRateThreshold(0.1)  // TODO need tuning
                                           .build(),
                CircuitBreakerRuleWithContent.onResponse((ctx, response) ->
                                                                 completedFuture(response.cause() != null)));
    }

    private Function<? super HttpClient, CircuitBreakerClient> newCircuitBreakerHttpDecorator() {
        return CircuitBreakerClient.newPerHostDecorator(
//        return CircuitBreakerHttpClient.newPerHostAndMethodDecorator(
                groupName -> CircuitBreaker.builder("frontend" + '_' + groupName)
                                           .listener(new MetricCollectingCircuitBreakerListener(meterRegistry))
                                           .failureRateThreshold(0.1)  // TODO need tuning
                                           .build(),
                // Armeria's ThrottlingService returns 429 while throttling
                CircuitBreakerRule.of(CircuitBreakerRule.onException(),
                                      CircuitBreakerRule.onServerErrorStatus(),
                                      CircuitBreakerRule.onStatus(HttpStatus.TOO_MANY_REQUESTS)));
    }

    private static RetryRuleWithContent<RpcResponse> newRetryRpcStrategy() {
        return (ctx, response, cause) -> {
            if (cause == null) {
                return completedFuture(RetryDecision.noRetry());
            } else {
                return completedFuture(RetryDecision.retry(Backoff.ofDefault()));
            }
        };
    }
}
