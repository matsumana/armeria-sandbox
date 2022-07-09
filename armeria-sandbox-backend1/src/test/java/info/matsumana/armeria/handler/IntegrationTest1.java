package info.matsumana.armeria.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.linecorp.armeria.client.Clients;
import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.common.AggregatedHttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.thrift.ThriftFuture;
import com.linecorp.armeria.server.Server;

import info.matsumana.armeria.TestContext;
import info.matsumana.armeria.thrift.Hello1Response;
import info.matsumana.armeria.thrift.Hello1Service;

@SpringJUnitConfig(TestContext.class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE,
        properties = "centraldogma.server.host=")
public class IntegrationTest1 {

    @Autowired
    private Server server;

    private WebClient client;
    private Hello1Service.AsyncIface hello1Service;

    @BeforeEach
    public void beforeEach() {
        final int port = server.activeLocalPort();
        client = WebClient.of("http://127.0.0.1:" + port);
        hello1Service = Clients.builder(String.format("tbinary+h2c://127.0.0.1:%d/thrift/hello1", port))
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
        final ThriftFuture<Hello1Response> future = new ThriftFuture<>();
        hello1Service.hello("foo", future);
        final Hello1Response res = future.get();
        assertThat(res.getMessage()).isEqualTo("Hello, foo");
    }
}
