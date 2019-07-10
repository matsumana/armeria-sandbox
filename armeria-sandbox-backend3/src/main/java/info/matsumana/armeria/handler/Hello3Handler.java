package info.matsumana.armeria.handler;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.linecorp.armeria.client.circuitbreaker.FailFastException;

import hu.akarnokd.rxjava2.interop.SingleInterop;
import info.matsumana.armeria.retrofit.HelloClient;
import info.matsumana.armeria.thrift.Hello3Service;
import retrofit2.Retrofit;

@Component
public class Hello3Handler implements Hello3Service.AsyncIface {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Retrofit retrofit;

    Hello3Handler(Retrofit retrofit) {
        this.retrofit = retrofit;
    }

    @Override
    public void hello(String name, AsyncMethodCallback<String> resultHandler) throws TException {
        final HelloClient helloClient = retrofit.create(HelloClient.class);
        SingleInterop.fromFuture(helloClient.hello(name))
                     .doOnSuccess(res -> log.debug("Retrofit HelloClient res={}", res))
                     .doOnError(e -> log.debug("Retrofit HelloClient exception", e))
                     .onErrorReturn(e -> {
                         if (e instanceof FailFastException || e.getCause() instanceof FailFastException) {
                             // Circuit Breaker fallback
                             return "[backend4 - fallback] Hello, ???";
                         }
                         throw new RuntimeException(e);
                     })
                     .map(s -> s + " & " + "[backend3] Hello, " + name)
                     .subscribe(resultHandler::onComplete,
                                e -> resultHandler.onError(new Exception(e)));
    }
}
