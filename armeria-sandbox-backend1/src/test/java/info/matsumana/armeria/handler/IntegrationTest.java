package info.matsumana.armeria.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.linecorp.armeria.client.ClientBuilder;
import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.common.AggregatedHttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.thrift.ThriftCompletableFuture;
import com.linecorp.armeria.server.Server;

import info.matsumana.armeria.TestContext;
import info.matsumana.armeria.thrift.Hello1Response;
import info.matsumana.armeria.thrift.Hello1Service;

@SpringJUnitConfig(TestContext.class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE,
        properties = "centraldogma.server.host=")
public class IntegrationTest {

    @Autowired
    private Server server;

    private HttpClient client;
    private Hello1Service.AsyncIface hello1Service;

    @BeforeEach
    public void beforeEach() {
        final int port = server.activePort().get().localAddress().getPort();
        client = HttpClient.of("http://127.0.0.1:" + port);
        hello1Service = new ClientBuilder(String.format("tbinary+h2c://127.0.0.1:%d/thrift/hello1", port))
                .build(Hello1Service.AsyncIface.class);
    }

    @Test
    public void healthCheck() throws Exception {
        final AggregatedHttpResponse res = client.get("/internal/healthcheck").aggregate().join();
        assertThat(res.status()).isEqualTo(HttpStatus.OK);
        assertThat(res.content().toStringUtf8()).isEqualTo("{\"healthy\":true}");
    }

    @Test
    public void docs() throws Exception {
        final AggregatedHttpResponse res = client.get("/internal/docs/").aggregate().join();
        assertThat(res.status()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void metrics() throws Exception {
        final AggregatedHttpResponse res = client.get("/internal/metrics").aggregate().join();
        assertThat(res.status()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void hello() throws Exception {
        final ThriftCompletableFuture<Hello1Response> future = new ThriftCompletableFuture<>();
        hello1Service.hello("foo", future);
        final Hello1Response res = future.get();
        assertThat(res.getMessage()).isEqualTo("Hello, foo");
    }
}
