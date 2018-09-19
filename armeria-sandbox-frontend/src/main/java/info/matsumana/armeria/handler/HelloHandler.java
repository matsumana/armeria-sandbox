package info.matsumana.armeria.handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.thrift.ThriftCompletableFuture;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;

import hu.akarnokd.rxjava2.interop.SingleInterop;
import info.matsumana.armeria.thrift.Hello1Service;
import info.matsumana.armeria.thrift.Hello2Service;
import info.matsumana.armeria.thrift.Hello3Service;
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
    public HttpResponse hello(@Param String name) throws TException {
        final ExecutorService threadPool = Executors.newFixedThreadPool(50);

        final ThriftCompletableFuture<String> future1 = new ThriftCompletableFuture<>();
        hello1Service.hello(name, future1);
        final Single<String> single1 = SingleInterop.fromFuture(future1);

        final ThriftCompletableFuture<String> future2 = new ThriftCompletableFuture<>();
        hello2Service.hello(name, future2);
        final Single<String> single2 = SingleInterop.fromFuture(future2);

        final ThriftCompletableFuture<String> future3 = new ThriftCompletableFuture<>();
        hello3Service.hello(name, future3);
        final Single<String> single3 = SingleInterop.fromFuture(future3);

        single1
                .map(res -> {
                    log.debug("hello1Service res={}", res);
                    return res;
                })
                .observeOn(Schedulers.from(threadPool))
                .zipWith(single2,
                         (res, res2) -> {
                             log.debug("hello2Service res={}", res2);
                             return res + ", " + res2;
                         })
                .observeOn(Schedulers.from(threadPool))
                .zipWith(single3,
                         (res, res2) -> {
                             log.debug("hello3Service res={}", res2);
                             return res + ", " + res2;
                         })
                .subscribe(res -> log.debug("res={}", res),
                           throwable -> log.error("cause is", throwable));

        log.debug("Exit HelloHandler#hello");

        return HttpResponse.of("[frontend] Hello, " + name);
    }
}
