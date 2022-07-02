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
import com.linecorp.armeria.spring.InternalServices;

import info.matsumana.armeria.TestContext;
import info.matsumana.armeria.grpc.Hello2.Hello2Request;
import info.matsumana.armeria.grpc.Hello2.Hello2Response;
import info.matsumana.armeria.grpc.Hello2ServiceGrpc.Hello2ServiceBlockingStub;

@SpringJUnitConfig(TestContext.class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE,
        properties = "centraldogma.server.host=")
public class IntegrationTest2 {

    @Autowired
    private InternalServices internalServices;

    private WebClient client;
    private Hello2ServiceBlockingStub hello2Service;

    @BeforeEach
    public void beforeEach() {
        final int port = internalServices.internalServicePort().getPort();
        client = WebClient.of("http://127.0.0.1:" + port);
        hello2Service = Clients.builder(String.format("gproto+h2c://127.0.0.1:%d/", port))
                               .build(Hello2ServiceBlockingStub.class);
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
        final Hello2Request request = Hello2Request.newBuilder()
                                                   .setName("foo")
                                                   .build();
        final Hello2Response response = hello2Service.hello(request);
        final String message = response.getMessage();
        assertThat(message).isEqualTo("Hello, foo");
    }
}
