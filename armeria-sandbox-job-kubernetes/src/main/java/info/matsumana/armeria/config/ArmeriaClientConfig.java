package info.matsumana.armeria.config;

import static com.linecorp.armeria.client.endpoint.EndpointSelectionStrategy.WEIGHTED_ROUND_ROBIN;

import java.util.regex.Pattern;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.client.Endpoint;
import com.linecorp.armeria.client.endpoint.EndpointGroup;
import com.linecorp.armeria.client.endpoint.EndpointGroupRegistry;
import com.linecorp.armeria.client.endpoint.StaticEndpointGroup;
import com.linecorp.armeria.client.endpoint.dns.DnsAddressEndpointGroupBuilder;
import com.linecorp.armeria.client.retrofit2.ArmeriaRetrofitBuilder;

import info.matsumana.armeria.config.ApiServerSetting.EndpointSetting;
import retrofit2.Retrofit;
import retrofit2.adapter.java8.Java8CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Configuration
public class ArmeriaClientConfig {

    // TODO IPv6
    private static final Pattern IPV4_REGEX =
            Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");

    private final ApiServerSetting apiServerSetting;

    ArmeriaClientConfig(ApiServerSetting apiServerSetting) {
        this.apiServerSetting = apiServerSetting;
    }

    @Bean
    Retrofit retrofit() {
        final EndpointSetting endpointSetting = apiServerSetting.getKubernetes();
        final String host = endpointSetting.getHost();
        final int port = endpointSetting.getPort();

        // In Kubernetes, we can't resolve server by host name with StaticEndpointGroup.
        // The route cause is in Netty's setting.
        // Therefore an exception occur as the following.
        //
        // io.netty.resolver.dns.DnsResolveContext$SearchDomainUnknownHostException:
        //   Search domain query failed. Original hostname: 'kubernetes.default.svc.cluster.local' failed to resolve 'kubernetes.default.svc.cluster.local.default.svc.cluster.local svc.cluster.local cluster.local' after 2 queries
        //
        // see also: https://github.com/netty/netty/issues/6559
        final EndpointGroup group;
        if (IPV4_REGEX.matcher(host).matches()) {
            group = new StaticEndpointGroup(Endpoint.of(endpointSetting.getHost(),
                                                        endpointSetting.getPort()));
        } else {
            group = new DnsAddressEndpointGroupBuilder(host)
                    .port(port)
                    .build();
        }

        EndpointGroupRegistry.register("job-kubernetes", group, WEIGHTED_ROUND_ROBIN);

        final String scheme = endpointSetting.getScheme();

        return new ArmeriaRetrofitBuilder()
                .baseUrl(String.format("%s://group:%s/", scheme, "job-kubernetes"))
                .addConverterFactory(ScalarsConverterFactory.create())
//                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(Java8CallAdapterFactory.create())
                .build();
    }
}
