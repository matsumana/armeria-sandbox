package info.matsumana.armeria.handler;

import java.util.concurrent.CompletableFuture;

import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;

import info.matsumana.armeria.service.HelloService;
import io.reactivex.Single;

@Component
public class HelloHandler {

    private final HelloService helloService;

    public HelloHandler(HelloService helloService) {
        this.helloService = helloService;
    }

    @Get("/hello/:name")
    public CompletableFuture<HttpResponse> hello(@Param String name) throws TException {

        // Convert to Single<HttpResponse>
        final Single<HttpResponse> singleResponse = helloService.hello(name)
                                                                .map(HttpResponse::of);

        // Convert to CompletableFuture
        final CompletableFuture<HttpResponse> futureResponse = new CompletableFuture<>();
        singleResponse.subscribe(futureResponse::complete, futureResponse::completeExceptionally);

        return futureResponse
                .exceptionally(e -> HttpResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                                                    MediaType.PLAIN_TEXT_UTF_8,
                                                    e.toString()));
    }
}
