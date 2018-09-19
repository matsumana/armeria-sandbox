package info.matsumana.armeria.thrift;

import java.util.concurrent.CompletableFuture;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import info.matsumana.armeria.retrofit.HelloClient;
import retrofit2.Retrofit;

@Component
public class HelloServiceImpl implements Hello3Service.AsyncIface {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Retrofit retrofit;

    HelloServiceImpl(Retrofit retrofit) {
        this.retrofit = retrofit;
    }

    @Override
    public void hello(String name, AsyncMethodCallback<String> resultHandler) throws TException {
        final HelloClient helloClient = retrofit.create(HelloClient.class);
        final CompletableFuture<String> future = helloClient.hello(name);

        future.thenAccept(res -> log.debug("Retrofit HelloClient res={}", res))
              .exceptionally(cause -> {
                  log.error("Retrofit HelloClient cause is", cause);
                  return null;
              });

        resultHandler.onComplete("Hello, " + name);
    }
}
