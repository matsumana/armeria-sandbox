package info.matsumana.armeria.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "api-servers")
public class ApiServerSetting {
    private String backend1;
    private String backend2;
    private String backend3;
    private String backend4;

    public String getBackend1() {
        return backend1;
    }

    public void setBackend1(String backend1) {
        this.backend1 = backend1;
    }

    public String getBackend2() {
        return backend2;
    }

    public void setBackend2(String backend2) {
        this.backend2 = backend2;
    }

    public String getBackend3() {
        return backend3;
    }

    public void setBackend3(String backend3) {
        this.backend3 = backend3;
    }

    public String getBackend4() {
        return backend4;
    }

    public void setBackend4(String backend4) {
        this.backend4 = backend4;
    }
}
