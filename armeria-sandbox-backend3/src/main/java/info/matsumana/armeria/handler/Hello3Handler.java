package info.matsumana.armeria.handler;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.linecorp.armeria.client.circuitbreaker.FailFastException;
import com.linecorp.armeria.common.util.SystemInfo;

import hu.akarnokd.rxjava2.interop.SingleInterop;
import info.matsumana.armeria.retrofit.HelloClient;
import info.matsumana.armeria.thrift.Hello3Response;
import info.matsumana.armeria.thrift.Hello3Service;
import info.matsumana.armeria.thrift.Hello4Response;
import retrofit2.Retrofit;

@Component
public class Hello3Handler implements Hello3Service.AsyncIface {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Retrofit retrofit;

    Hello3Handler(Retrofit retrofit) {
        this.retrofit = retrofit;
    }

    @Override
    public void hello(String name, AsyncMethodCallback<Hello3Response> resultHandler) throws TException {
        final HelloClient helloClient = retrofit.create(HelloClient.class);
        SingleInterop.fromFuture(helloClient.hello(name))
                     .doOnSuccess(res -> log.debug("Retrofit HelloClient res={}", res))
                     .doOnError(e -> log.debug("Retrofit HelloClient exception", e))
                     .onErrorReturn(e -> {
                         if (e instanceof FailFastException || e.getCause() instanceof FailFastException) {
                             // Circuit Breaker fallback
                             return new Hello4Response("", "Hello, ???");
                         }
                         throw new RuntimeException(e);
                     })
                     .map(response -> new Hello3Response(SystemInfo.hostname(),
                                                         "Hello, " + name,
                                                         new Hello4Response(response.getServerName(),
                                                                            response.getMessage()))
                     )
                     .subscribe(resultHandler::onComplete,
                                e -> resultHandler.onError(new Exception(e)));
    }
}
