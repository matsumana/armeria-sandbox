package info.matsumana.armeria;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.linecorp.armeria.server.Server;

import brave.internal.Nullable;
import info.matsumana.armeria.config.ApiServerConfig;

@SpringBootApplication
public class TestContext {

    // Mock beans will be defined here

    @Bean
    @Primary
    ApiServerConfig apiServerConfig(@Nullable Server server) {
        return new ApiServerConfig("", server);
    }
}
