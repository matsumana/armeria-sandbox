package info.matsumana.armeria.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.linecorp.armeria.client.ClientBuilder;
import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.common.AggregatedHttpMessage;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.server.Server;

import info.matsumana.armeria.TestContext;
import info.matsumana.armeria.thrift.HelloService;

@SpringJUnitConfig(TestContext.class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
public class IntegrationTest {

    @Autowired
    private Server server;

    private HttpClient client;
    private HelloService.Iface helloService;

    @BeforeEach
    public void beforeEach() {
        client = HttpClient.of("http://127.0.0.1:" + server.activePort().get().localAddress().getPort());
        helloService = new ClientBuilder(
                String.format("tbinary+h2c://127.0.0.1:%d/thrift/hello",
                              server.activePort().get().localAddress().getPort()))
                .build(HelloService.Iface.class);
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
        final String res = helloService.hello("foo");
        assertThat(res).isEqualTo("Hello, foo");
    }
}
