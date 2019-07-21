package info.matsumana.armeria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.linecorp.armeria.client.retrofit2.ArmeriaRetrofitBuilder;

import info.matsumana.armeria.config.ApiServerSetting.EndpointSetting;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
public class ArmeriaClientConfig {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final ApiServerSetting apiServerSetting;

    ArmeriaClientConfig(ApiServerSetting apiServerSetting) {
        this.apiServerSetting = apiServerSetting;
    }

    @Bean
    Retrofit retrofit() {
        final EndpointSetting endpointSetting = apiServerSetting.getKubernetes();
        final String scheme = endpointSetting.getScheme();
        final String host = endpointSetting.getHost();
        final int port = endpointSetting.getPort();

        return new ArmeriaRetrofitBuilder()
                .baseUrl(String.format("%s://%s:%d/", scheme, host, port))
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .build();
    }
}
