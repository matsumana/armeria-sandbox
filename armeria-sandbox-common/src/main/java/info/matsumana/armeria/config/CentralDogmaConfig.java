package info.matsumana.armeria.config;

import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import com.linecorp.centraldogma.client.CentralDogma;
import com.linecorp.centraldogma.client.armeria.ArmeriaCentralDogmaBuilder;

@Configuration
public class CentralDogmaConfig {

    @Bean
    @Nullable
    CentralDogma centralDogma(@Value("${centraldogma.server.host:}") String host,
                              @Value("${centraldogma.server.port:0}") int port) throws UnknownHostException {
        if (host == null || host.isEmpty()) {
            return null;
        } else {
            ArmeriaCentralDogmaBuilder builder = new ArmeriaCentralDogmaBuilder();
            if (port >= 1) {
                builder = builder.host(host, port);
            } else {
                builder = builder.host(host);
            }

            return builder.build();
        }
    }
}
