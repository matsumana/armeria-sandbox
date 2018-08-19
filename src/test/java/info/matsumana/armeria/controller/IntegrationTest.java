package info.matsumana.armeria.controller;

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

import info.matsumana.armeria.TextContext;

@SpringJUnitConfig(TextContext.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
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
    public void index() {
        final AggregatedHttpMessage res = client.get("/").aggregate().join();
        assertThat(res.status()).isEqualTo(HttpStatus.OK);
        assertThat(res.content().toStringUtf8()).isEqualTo("index");
    }

    @Test
    public void hello() throws Exception {
        final AggregatedHttpMessage res = client.get("/hello").aggregate().join();
        assertThat(res.status()).isEqualTo(HttpStatus.OK);
        assertThat(res.content().toStringUtf8()).isEqualTo("Hello, World");
    }

    @Test
    public void hello_thrift() throws Exception {
        final AggregatedHttpMessage res = client.get("/hello/bar").aggregate().join();
        assertThat(res.status()).isEqualTo(HttpStatus.OK);
        assertThat(res.content().toStringUtf8()).isEqualTo("Hello, bar");
    }
}
