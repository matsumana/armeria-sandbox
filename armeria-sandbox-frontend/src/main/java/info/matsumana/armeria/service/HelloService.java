package info.matsumana.armeria.service;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.ListenableFuture;

import com.linecorp.armeria.client.circuitbreaker.FailFastException;
import com.linecorp.armeria.common.thrift.ThriftCompletableFuture;

import hu.akarnokd.rxjava2.interop.SingleInterop;
import info.matsumana.armeria.bean.FrontendResponse;
import info.matsumana.armeria.bean.handler.HelloResponse;
import info.matsumana.armeria.grpc.Hello2.Hello2Request;
import info.matsumana.armeria.grpc.Hello2.Hello2Response;
import info.matsumana.armeria.grpc.Hello2ServiceGrpc.Hello2ServiceFutureStub;
import info.matsumana.armeria.thrift.Hello1Response;
import info.matsumana.armeria.thrift.Hello1Service;
import info.matsumana.armeria.thrift.Hello3Response;
import info.matsumana.armeria.thrift.Hello3Service;
import info.matsumana.armeria.thrift.Hello4Response;
import info.matsumana.armeria.util.SingleInteropUtil;
import io.grpc.StatusRuntimeException;
import io.reactivex.Single;

@Service
public class HelloService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Hello1Service.AsyncIface hello1Service;
    private final Hello2ServiceFutureStub hello2Service;
    private final Hello3Service.AsyncIface hello3Service;

    public HelloService(Hello1Service.AsyncIface hello1Service,
                        Hello2ServiceFutureStub hello2Service,
                        Hello3Service.AsyncIface hello3Service) {
        this.hello1Service = hello1Service;
        this.hello2Service = hello2Service;
        this.hello3Service = hello3Service;
    }

    public Single<FrontendResponse> hello(String name) throws TException {

        // Convert to Single
        final ThriftCompletableFuture<Hello1Response> future1 = new ThriftCompletableFuture<>();
        hello1Service.hello(name, future1);

        final Hello2Request request = Hello2Request.newBuilder()
                                                   .setName(name)
                                                   .build();
        final ListenableFuture<Hello2Response> future2 = hello2Service.hello(request);

        final ThriftCompletableFuture<Hello3Response> future3 = new ThriftCompletableFuture<>();
        hello3Service.hello(name, future3);

        return Single.zip(SingleInterop.fromFuture(future1)
                                       .doOnSuccess(res -> log.debug("hello1Service res={}", res))
                                       .doOnError(e -> log.debug("hello1Service exception", e))
                                       .onErrorReturn(e -> {
                                           if (e instanceof FailFastException) {
                                               // Circuit Breaker fallback
                                               return new Hello1Response("", "Hello, ???");
                                           }
                                           throw new RuntimeException(e);
                                       })
                                       .map(res -> {
                                           final HelloResponse response = new HelloResponse();
                                           response.setServerName(res.getServerName());
                                           response.setMessage(res.getMessage());
                                           return response;
                                       }),
                          SingleInteropUtil.fromListenableFuture(future2)
                                           .doOnSuccess(res -> log.debug("hello2Service res={}", res))
                                           .doOnError(e -> log.debug("hello2Service exception", e))
                                           .onErrorReturn(e -> {
                                               if (e instanceof StatusRuntimeException) {
                                                   final Throwable cause = e.getCause();
                                                   if (cause instanceof FailFastException) {
                                                       // Circuit Breaker fallback
                                                       return Hello2Response
                                                               .newBuilder()
                                                               .setServerName("")
                                                               .setMessage("Hello, ???")
                                                               .build();
                                                   }
                                                   throw new RuntimeException(cause);
                                               }
                                               throw new RuntimeException(e);
                                           })
                                           .map(res -> {
                                               final HelloResponse response = new HelloResponse();
                                               response.setServerName(res.getServerName());
                                               response.setMessage(res.getMessage());
                                               return response;
                                           }),
                          SingleInterop.fromFuture(future3)
                                       .doOnSuccess(res -> log.debug("hello3Service res={}", res))
                                       .doOnError(e -> log.debug("hello3Service exception", e))
                                       .onErrorReturn(e -> {
                                           if (e instanceof FailFastException) {
                                               // Circuit Breaker fallback
                                               return new Hello3Response("", "Hello, ???",
                                                                         new Hello4Response("", ""));
                                           }
                                           throw new RuntimeException(e);
                                       }),
                          (res1, res2, res3) -> {
                              final FrontendResponse response = new FrontendResponse();
                              response.setBackend1(res1);
                              response.setBackend2(res2);

                              final HelloResponse backend3 = new HelloResponse();
                              backend3.setServerName(res3.getServerName());
                              backend3.setMessage(res3.getMessage());
                              response.setBackend3(backend3);

                              final Hello4Response res4 = res3.getHello4Response();
                              final HelloResponse backend4 = new HelloResponse();
                              backend4.setServerName(res4.getServerName());
                              backend4.setMessage(res4.getMessage());
                              response.setBackend4(backend4);

                              return response;
                          });
    }
}
