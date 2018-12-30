package info.matsumana.armeria.config;

import static com.linecorp.armeria.client.endpoint.EndpointSelectionStrategy.WEIGHTED_ROUND_ROBIN;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.client.Client;
import com.linecorp.armeria.client.UnprocessedRequestException;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerBuilder;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerHttpClient;
import com.linecorp.armeria.client.circuitbreaker.MetricCollectingCircuitBreakerListener;
import com.linecorp.armeria.client.endpoint.EndpointGroup;
import com.linecorp.armeria.client.endpoint.EndpointGroupRegistry;
import com.linecorp.armeria.client.endpoint.healthcheck.HttpHealthCheckedEndpointGroup;
import com.linecorp.armeria.client.endpoint.healthcheck.HttpHealthCheckedEndpointGroupBuilder;
import com.linecorp.armeria.client.logging.LoggingClient;
import com.linecorp.armeria.client.retrofit2.ArmeriaRetrofitBuilder;
import com.linecorp.armeria.client.retry.RetryStrategy;
import com.linecorp.armeria.client.retry.RetryingHttpClient;
import com.linecorp.armeria.client.tracing.HttpTracingClient;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.HttpStatusClass;

import brave.Tracing;
import info.matsumana.armeria.helper.EndpointGroupHelper;
import io.micrometer.core.instrument.MeterRegistry;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

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
        tracing = tracingFactory.create("backend3");
    }

    @Bean
    Retrofit retrofit() {
        final EndpointGroup group = endpointGroupHelper.newEndpointGroup("/backend4.json",
                                                                         apiServerSetting.getBackend4());
        final HttpHealthCheckedEndpointGroup healthCheckedGroup =
                new HttpHealthCheckedEndpointGroupBuilder(group, "/internal/healthcheck")
                        .build();
        if (EndpointGroupRegistry.register("backend4", healthCheckedGroup, WEIGHTED_ROUND_ROBIN)) {
            healthCheckedGroup.newMeterBinder("backend4").bindTo(meterRegistry);
        }

        return new ArmeriaRetrofitBuilder()
                .baseUrl(String.format("http://group:%s/", "backend4"))
                .addConverterFactory(ScalarsConverterFactory.create())
//                .addConverterFactory(JacksonConverterFactory.create())
                .withClientOptions((uri, optionsBuilder) -> optionsBuilder
                        .decorator(newCircuitBreakerDecorator())
                        .decorator(HttpTracingClient.newDecorator(tracing, "backend4"))
                        .decorator(LoggingClient.newDecorator())
                        .decorator(RetryingHttpClient.newDecorator(RetryStrategy.onServerErrorStatus(),
                                                                   MAX_TOTAL_ATTEMPTS)))
                .build();
    }

    private Function<Client<HttpRequest, HttpResponse>, CircuitBreakerHttpClient> newCircuitBreakerDecorator() {
        return CircuitBreakerHttpClient.newPerHostDecorator(
//        return CircuitBreakerHttpClient.newPerHostAndMethodDecorator(
                groupName -> new CircuitBreakerBuilder("backend3" + '_' + groupName)
                        .listener(new MetricCollectingCircuitBreakerListener(meterRegistry))
                        .failureRateThreshold(0.1)  // TODO need tuning
                        .build(),
                (ctx, cause) -> {
                    if (cause != null) {
                        if (cause instanceof UnprocessedRequestException) {
                            // Neither a success nor a failure because the request has not been handled by the server.
                            return CompletableFuture.completedFuture(null);
                        }
                        // A failure if an Exception is raised.
                        return CompletableFuture.completedFuture(false);
                    }

                    final HttpStatus status = ctx.log().responseHeaders().status();
                    if (status != null) {
                        // A failure if the response is 5xx.
                        if (status.codeClass() == HttpStatusClass.SERVER_ERROR) {
                            return CompletableFuture.completedFuture(false);
                        }

                        // A success if the response is 2xx.
                        if (status.codeClass() == HttpStatusClass.SUCCESS) {
                            return CompletableFuture.completedFuture(true);
                        }
                    }

                    // Neither a success nor a failure. Do not take this response into account.
                    return CompletableFuture.completedFuture(null);
                });
    }
}
