package info.matsumana.armeria.config;

import static com.linecorp.armeria.client.endpoint.EndpointSelectionStrategy.WEIGHTED_ROUND_ROBIN;
import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.client.Client;
import com.linecorp.armeria.client.Endpoint;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerBuilder;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerHttpClient;
import com.linecorp.armeria.client.circuitbreaker.MetricCollectingCircuitBreakerListener;
import com.linecorp.armeria.client.endpoint.EndpointGroup;
import com.linecorp.armeria.client.endpoint.EndpointGroupRegistry;
import com.linecorp.armeria.client.endpoint.StaticEndpointGroup;
import com.linecorp.armeria.client.endpoint.healthcheck.HttpHealthCheckedEndpointGroup;
import com.linecorp.armeria.client.endpoint.healthcheck.HttpHealthCheckedEndpointGroupBuilder;
import com.linecorp.armeria.client.retrofit2.ArmeriaRetrofitBuilder;
import com.linecorp.armeria.client.tracing.HttpTracingClient;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;

import brave.Tracing;
import io.micrometer.core.instrument.MeterRegistry;
import retrofit2.Retrofit;
import retrofit2.adapter.java8.Java8CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

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
    Retrofit retrofit() {
        final EndpointGroup group =
                new StaticEndpointGroup(apiServerSetting.getBackend4().stream()
                                                        .map(setting -> Endpoint.of(setting.getHost(),
                                                                                    setting.getPort()))
                                                        .collect(toUnmodifiableList()));
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
                .addCallAdapterFactory(Java8CallAdapterFactory.create())
                .withClientOptions((uri, optionsBuilder) -> optionsBuilder
                        .decorator(HttpRequest.class, HttpResponse.class,
                                   HttpTracingClient.newDecorator(tracing, "backend4"))
                        .decorator(HttpRequest.class, HttpResponse.class,
                                   newCircuitBreakerDecorator()))
                .build();
    }

    private Function<Client<HttpRequest, HttpResponse>, CircuitBreakerHttpClient> newCircuitBreakerDecorator() {
        return CircuitBreakerHttpClient.newPerHostDecorator(
//        return CircuitBreakerRpcClient.newPerHostAndMethodDecorator(
                groupName -> new CircuitBreakerBuilder("backend3" + '_' + groupName)
                        .listener(new MetricCollectingCircuitBreakerListener(meterRegistry))
//                        .failureRateThreshold(0.1)
                        .build(),
                response -> response.completionFuture()
                                    .handle((res, cause) -> cause == null));
    }
}
