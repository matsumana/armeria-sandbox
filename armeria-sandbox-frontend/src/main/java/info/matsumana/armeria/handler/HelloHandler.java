package info.matsumana.armeria.handler;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.thrift.ThriftCompletableFuture;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;

import info.matsumana.armeria.thrift.Hello1Service;
import info.matsumana.armeria.thrift.Hello2Service;
import info.matsumana.armeria.thrift.Hello3Service;

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
        {
            final ThriftCompletableFuture<String> future = new ThriftCompletableFuture<>();
            hello1Service.hello(name, future);
            future.thenAccept(res -> log.debug("hello1Service res={}", res))
                  .exceptionally(cause -> {
                      log.error("hello1Service cause is", cause);
                      return null;
                  });
        }

        {
            final ThriftCompletableFuture<String> future = new ThriftCompletableFuture<>();
            hello2Service.hello(name, future);
            future.thenAccept(res -> log.debug("hello2Service res={}", res))
                  .exceptionally(cause -> {
                      log.error("hello2Service cause is", cause);
                      return null;
                  });
        }

        {
            final ThriftCompletableFuture<String> future = new ThriftCompletableFuture<>();
            hello3Service.hello(name, future);
            future.thenAccept(res -> log.debug("hello3Service res={}", res))
                  .exceptionally(cause -> {
                      log.error("hello3Service cause is", cause);
                      return null;
                  });
        }

        return HttpResponse.of("Hello, " + name);
    }
}
