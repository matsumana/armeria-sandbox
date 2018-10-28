package info.matsumana.armeria.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.common.AggregatedHttpMessage;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.server.Server;

import info.matsumana.armeria.TestContext;

@SpringJUnitConfig(TestContext.class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE,
        properties = "centraldogma.server.host=")
public class IntegrationTest {

    @Autowired
    private Server server;

    private HttpClient client;

    @BeforeEach
    public void beforeEach() {
        client = HttpClient.of("http://127.0.0.1:" + server.activePort().get().localAddress().getPort());
    }

    @Test
    public void healthCheck() throws Exception {
        final AggregatedHttpMessage res = client.get("/internal/healthcheck").aggregate().join();
        assertThat(res.status()).isEqualTo(HttpStatus.OK);
        assertThat(res.content().toStringUtf8()).isEqualTo("ok");
    }

    @Test
    public void docs() throws Exception {
        final AggregatedHttpMessage res = client.get("/internal/docs/").aggregate().join();
        assertThat(res.status()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void metrics() throws Exception {
        final AggregatedHttpMessage res = client.get("/internal/metrics").aggregate().join();
        assertThat(res.status()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void hello() throws Exception {
        final AggregatedHttpMessage res = client.get("/hello/bar").aggregate().join();
        assertThat(res.status()).isEqualTo(HttpStatus.OK);
        assertThat(res.content().toStringUtf8()).isEqualTo("[backend4] Hello, bar");
    }
}
