package info.matsumana.armeria.config;

import static com.linecorp.armeria.client.endpoint.EndpointSelectionStrategy.WEIGHTED_ROUND_ROBIN;
import static java.util.stream.Collectors.toUnmodifiableList;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.client.Endpoint;
import com.linecorp.armeria.client.endpoint.EndpointGroup;
import com.linecorp.armeria.client.endpoint.EndpointGroupRegistry;
import com.linecorp.armeria.client.endpoint.StaticEndpointGroup;
import com.linecorp.armeria.client.endpoint.healthcheck.HttpHealthCheckedEndpointGroup;
import com.linecorp.armeria.client.endpoint.healthcheck.HttpHealthCheckedEndpointGroupBuilder;
import com.linecorp.armeria.client.retrofit2.ArmeriaRetrofitBuilder;

import io.micrometer.core.instrument.MeterRegistry;
import retrofit2.Retrofit;
import retrofit2.adapter.java8.Java8CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Configuration
public class ArmeriaClientConfig {

    private final ApiServerSetting apiServerSetting;
    private final MeterRegistry meterRegistry;

    ArmeriaClientConfig(ApiServerSetting apiServerSetting, MeterRegistry meterRegistry) {
        this.apiServerSetting = apiServerSetting;
        this.meterRegistry = meterRegistry;
    }

    @Bean
    Retrofit retrofit() {
        final EndpointGroup group =
                new StaticEndpointGroup(apiServerSetting
                                                .getKubernetes().stream()
                                                .map(setting -> Endpoint.of(setting.getHost(),
                                                                            setting.getPort()))
                                                .collect(toUnmodifiableList()));

        final HttpHealthCheckedEndpointGroup healthCheckedGroup =
                new HttpHealthCheckedEndpointGroupBuilder(group, "/healthz")
                        .build();
        if (EndpointGroupRegistry.register("kubernetes", healthCheckedGroup, WEIGHTED_ROUND_ROBIN)) {
            healthCheckedGroup.newMeterBinder("kubernetes").bindTo(meterRegistry);
        }

        return new ArmeriaRetrofitBuilder()
                .baseUrl(String.format("http://group:%s/", "kubernetes"))
                .addConverterFactory(ScalarsConverterFactory.create())
//                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(Java8CallAdapterFactory.create())
                .build();
    }
}
