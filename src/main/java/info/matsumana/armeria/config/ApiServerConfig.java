package info.matsumana.armeria.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import com.google.common.base.Strings;

import com.linecorp.armeria.server.Server;

@Configuration
public class ApiServerConfig {

    private final String thriftServer;

    public ApiServerConfig(@Value("${api-server}") String apiServer,
                           @Nullable Server server) {
        if (!Strings.isNullOrEmpty(apiServer)) {
            thriftServer = apiServer;
        } else {
            if (server != null) {
                thriftServer = "127.0.0.1:" + server.activePort().get().localAddress().getPort();
            } else {
                thriftServer = "";
            }
        }
    }

    public String getThriftServer() {
        return thriftServer;
    }
}
