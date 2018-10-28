package info.matsumana.armeria.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "api-servers")
public class ApiServerSetting {

    public static class EndpointSetting {
        private String host;
        private int port;
        private String scheme;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getScheme() {
            return scheme;
        }

        public void setScheme(String scheme) {
            this.scheme = scheme;
        }
    }

    private List<EndpointSetting> kubernetes;
    private List<EndpointSetting> backend1;
    private List<EndpointSetting> backend2;
    private List<EndpointSetting> backend3;
    private List<EndpointSetting> backend4;

    public List<EndpointSetting> getKubernetes() {
        return kubernetes;
    }

    public void setKubernetes(List<EndpointSetting> kubernetes) {
        this.kubernetes = kubernetes;
    }

    public List<EndpointSetting> getBackend1() {
        return backend1;
    }

    public void setBackend1(List<EndpointSetting> backend1) {
        this.backend1 = backend1;
    }

    public List<EndpointSetting> getBackend2() {
        return backend2;
    }

    public void setBackend2(List<EndpointSetting> backend2) {
        this.backend2 = backend2;
    }

    public List<EndpointSetting> getBackend3() {
        return backend3;
    }

    public void setBackend3(List<EndpointSetting> backend3) {
        this.backend3 = backend3;
    }

    public List<EndpointSetting> getBackend4() {
        return backend4;
    }

    public void setBackend4(List<EndpointSetting> backend4) {
        this.backend4 = backend4;
    }
}
