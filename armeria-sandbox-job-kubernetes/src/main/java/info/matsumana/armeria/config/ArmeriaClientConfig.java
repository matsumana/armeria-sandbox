package info.matsumana.armeria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.client.retrofit2.ArmeriaRetrofitBuilder;

import info.matsumana.armeria.config.ApiServerSetting.EndpointSetting;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Configuration
public class ArmeriaClientConfig {

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
                .addConverterFactory(ScalarsConverterFactory.create())
//                .addConverterFactory(JacksonConverterFactory.create())
                .build();
    }
}
