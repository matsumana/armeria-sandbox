package info.matsumana.armeria.config;

import static com.linecorp.armeria.client.endpoint.EndpointSelectionStrategy.WEIGHTED_ROUND_ROBIN;
import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.client.Endpoint;
import com.linecorp.armeria.client.endpoint.EndpointGroup;
import com.linecorp.armeria.client.endpoint.EndpointGroupRegistry;
import com.linecorp.armeria.client.endpoint.StaticEndpointGroup;
import com.linecorp.armeria.client.retrofit2.ArmeriaRetrofitBuilder;

import info.matsumana.armeria.config.ApiServerSetting.EndpointSetting;
import retrofit2.Retrofit;
import retrofit2.adapter.java8.Java8CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Configuration
public class ArmeriaClientConfig {

    private final ApiServerSetting apiServerSetting;

    ArmeriaClientConfig(ApiServerSetting apiServerSetting) {
        this.apiServerSetting = apiServerSetting;
    }

    @Bean
    Retrofit retrofit() {
        final List<EndpointSetting> kubernetesEndpoints = apiServerSetting.getKubernetes();
        if (kubernetesEndpoints.stream().distinct().count() != 1) {
            throw new IllegalArgumentException("scheme setting error");
        }

        final EndpointGroup group =
                new StaticEndpointGroup(kubernetesEndpoints.stream()
                                                           .map(setting -> Endpoint.of(setting.getHost(),
                                                                                       setting.getPort()))
                                                           .collect(toUnmodifiableList()));
        EndpointGroupRegistry.register("job-kubernetes", group, WEIGHTED_ROUND_ROBIN);

        final String scheme = kubernetesEndpoints.stream()
                                                 .map(EndpointSetting::getScheme)
                                                 .findFirst()
                                                 .orElse("http");

        return new ArmeriaRetrofitBuilder()
                .baseUrl(String.format("%s://group:%s/", scheme, "job-kubernetes"))
                .addConverterFactory(ScalarsConverterFactory.create())
//                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(Java8CallAdapterFactory.create())
                .build();
    }
}
