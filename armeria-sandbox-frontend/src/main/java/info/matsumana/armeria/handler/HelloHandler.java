package info.matsumana.armeria.handler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import com.linecorp.armeria.client.circuitbreaker.FailFastException;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.common.thrift.ThriftCompletableFuture;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;

import hu.akarnokd.rxjava2.interop.SingleInterop;
import info.matsumana.armeria.thrift.Hello1Service;
import info.matsumana.armeria.thrift.Hello2Service;
import info.matsumana.armeria.thrift.Hello3Service;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

@Component
public class HelloHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Hello1Service.AsyncIface hello1Service;
    private final Hello2Service.AsyncIface hello2Service;
    private final Hello3Service.AsyncIface hello3Service;

    HelloHandler(Hello1Service.AsyncIface hello1Service,
                 Hello2Service.AsyncIface hello2Service,
                 Hello3Service.AsyncIface hello3Service) {
        this.hello1Service = hello1Service;
        this.hello2Service = hello2Service;
        this.hello3Service = hello3Service;
    }

    @Get("/hello/:name")
    public CompletableFuture<HttpResponse> hello(@Param String name) throws TException {
        final ExecutorService threadPool =
                Executors.newFixedThreadPool(50,
                                             new ThreadFactoryBuilder()
                                                     .setNameFormat("rxjava-executor-%d")
                                                     .build());
        final ExecutorService monitoredThreadPool = ExecutorServiceMetrics.monitor(Metrics.globalRegistry,
                                                                                   threadPool,
                                                                                   "rxjavaExecutor");

        // Convert to Single
        final ThriftCompletableFuture<String> future1 = new ThriftCompletableFuture<>();
        hello1Service.hello(name, future1);
        final Single<String> single1 = SingleInterop.fromFuture(future1);

        final ThriftCompletableFuture<String> future2 = new ThriftCompletableFuture<>();
        hello2Service.hello(name, future2);
        final Single<String> single2 = SingleInterop.fromFuture(future2);

        final ThriftCompletableFuture<String> future3 = new ThriftCompletableFuture<>();
        hello3Service.hello(name, future3);
        final Single<String> single3 = SingleInterop.fromFuture(future3);

        // Convert to Single<HttpResponse>
        final Single<HttpResponse> singleResponse = single1
                .doOnSuccess(res -> log.debug("hello1Service res={}", res))
                .doOnError(e -> log.debug("hello1Service exception", e))
                .onErrorReturn(e -> {
                    if (e instanceof FailFastException) {
                        // fallback
                        return "[backend1 - fallback] Hello, ???";
                    }
                    throw new RuntimeException(e);
                })
                //
                .observeOn(Schedulers.from(monitoredThreadPool))
                .zipWith(single2, (res, res2) -> res + " & " + res2)
                .doOnSuccess(res -> log.debug("hello2Service res={}", res))
                .doOnError(e -> log.debug("hello2Service exception", e))
                .onErrorReturn(e -> {
                    if (e instanceof FailFastException) {
                        // fallback
                        return "[backend2 - fallback] Hello, ???";
                    }
                    throw new RuntimeException(e);
                })
                //
                .observeOn(Schedulers.from(monitoredThreadPool))
                .zipWith(single3, (res, res2) -> res + " & " + res2)
                .doOnSuccess(res -> log.debug("hello3Service res={}", res))
                .doOnError(e -> log.debug("hello3Service exception", e))
                .onErrorReturn(e -> {
                    if (e instanceof FailFastException) {
                        // fallback
                        return "[backend3 - fallback] Hello, ???";
                    }
                    throw new RuntimeException(e);
                })
                //
                .map(HttpResponse::of);

        // Convert to CompletableFuture
        final CompletableFuture<HttpResponse> futureResponse = new CompletableFuture<>();
        singleResponse.subscribe(futureResponse::complete, futureResponse::completeExceptionally);

        return futureResponse
                .exceptionally(e -> HttpResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                                                    MediaType.JSON_UTF_8,
                                                    e.toString()));
    }
}
