package info.matsumana.armeria.config;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.client.ClientDecoration;
import com.linecorp.armeria.client.ClientDecorationBuilder;
import com.linecorp.armeria.client.ClientOptionValue;
import com.linecorp.armeria.client.ClientOptions;
import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.client.brave.BraveClient;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreaker;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerClient;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerListener;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerRule;
import com.linecorp.armeria.client.endpoint.EndpointGroup;
import com.linecorp.armeria.client.endpoint.healthcheck.HealthCheckedEndpointGroup;
import com.linecorp.armeria.client.logging.LoggingClient;
import com.linecorp.armeria.client.retrofit2.ArmeriaRetrofit;
import com.linecorp.armeria.client.retry.RetryRule;
import com.linecorp.armeria.client.retry.RetryingClient;

import brave.Tracing;
import info.matsumana.armeria.helper.EndpointGroupHelper;
import io.micrometer.core.instrument.MeterRegistry;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

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
        final HealthCheckedEndpointGroup healthCheckedGroup = HealthCheckedEndpointGroup.of(group,
                                                                                            "/internal/healthcheck");
        healthCheckedGroup.newMeterBinder("backend4").bindTo(meterRegistry);

        return ArmeriaRetrofit.builder("http", healthCheckedGroup)
                              .addConverterFactory(JacksonConverterFactory.create())
                              .option(createDecorationOption())
                              .build();
    }

    private ClientOptionValue<ClientDecoration> createDecorationOption() {
        final ClientDecorationBuilder clientDecorationBuilder = ClientDecoration.builder();
        final ClientDecoration clientDecoration =
                clientDecorationBuilder.add(BraveClient.newDecorator(tracing, "backend4"))
                                       .add(RetryingClient.newDecorator(RetryRule.onServerErrorStatus(),
                                                                        MAX_TOTAL_ATTEMPTS))
                                       .add(newCircuitBreakerHttpDecorator())
                                       .add(LoggingClient.newDecorator())
                                       .build();
        return ClientOptions.DECORATION.newValue(clientDecoration);
    }

    private Function<? super HttpClient, CircuitBreakerClient> newCircuitBreakerHttpDecorator() {
        return CircuitBreakerClient.newPerHostDecorator(
//        return CircuitBreakerClient.newPerHostAndMethodDecorator(
                groupName -> CircuitBreaker.builder("backend3" + '_' + groupName)
                                           .listener(CircuitBreakerListener.metricCollecting(meterRegistry))
                                           .failureRateThreshold(0.1)  // TODO need tuning
                                           .build(),
                CircuitBreakerRule.onServerErrorStatus());
    }
}
