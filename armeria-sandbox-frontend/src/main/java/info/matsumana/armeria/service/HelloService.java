package info.matsumana.armeria.service;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.linecorp.armeria.client.circuitbreaker.FailFastException;
import com.linecorp.armeria.common.thrift.ThriftCompletableFuture;

import hu.akarnokd.rxjava2.interop.SingleInterop;
import info.matsumana.armeria.thrift.Hello1Service;
import info.matsumana.armeria.thrift.Hello2Service;
import info.matsumana.armeria.thrift.Hello3Service;
import io.reactivex.Single;

@Service
public class HelloService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Hello1Service.AsyncIface hello1Service;
    private final Hello2Service.AsyncIface hello2Service;
    private final Hello3Service.AsyncIface hello3Service;

    public HelloService(Hello1Service.AsyncIface hello1Service,
                        Hello2Service.AsyncIface hello2Service,
                        Hello3Service.AsyncIface hello3Service) {
        this.hello1Service = hello1Service;
        this.hello2Service = hello2Service;
        this.hello3Service = hello3Service;
    }

    public Single<String> hello(String name) throws TException {

        // Convert to Single
        final ThriftCompletableFuture<String> future1 = new ThriftCompletableFuture<>();
        hello1Service.hello(name, future1);

        final ThriftCompletableFuture<String> future2 = new ThriftCompletableFuture<>();
        hello2Service.hello(name, future2);

        final ThriftCompletableFuture<String> future3 = new ThriftCompletableFuture<>();
        hello3Service.hello(name, future3);

        return Single.zip(SingleInterop.fromFuture(future1)
                                       .doOnSuccess(res -> log.debug("hello1Service res={}", res))
                                       .doOnError(e -> log.debug("hello1Service exception", e))
                                       .onErrorReturn(e -> {
                                           if (e instanceof FailFastException) {
                                               // Circuit Breaker fallback
                                               return "[backend1 - fallback] Hello, ???";
                                           }
                                           throw new RuntimeException(e);
                                       }),
                          SingleInterop.fromFuture(future2)
                                       .doOnSuccess(res -> log.debug("hello2Service res={}", res))
                                       .doOnError(e -> log.debug("hello2Service exception", e))
                                       .onErrorReturn(e -> {
                                           if (e instanceof FailFastException) {
                                               // Circuit Breaker fallback
                                               return "[backend2 - fallback] Hello, ???";
                                           }
                                           throw new RuntimeException(e);
                                       }),
                          SingleInterop.fromFuture(future3)
                                       .doOnSuccess(res -> log.debug("hello3Service res={}", res))
                                       .doOnError(e -> log.debug("hello3Service exception", e))
                                       .onErrorReturn(e -> {
                                           if (e instanceof FailFastException) {
                                               // Circuit Breaker fallback
                                               return "[backend3 - fallback] Hello, ???";
                                           }
                                           throw new RuntimeException(e);
                                       }),
                          (s1, s2, s3) -> String.format("%s & %s & %s", s1, s2, s3));
    }
}
