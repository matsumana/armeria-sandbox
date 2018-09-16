package info.matsumana.armeria.thrift;

import java.util.concurrent.ExecutionException;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import info.matsumana.armeria.retrofit.HelloClient;
import retrofit2.Retrofit;

@Component
public class HelloServiceImpl implements Hello3Service.Iface {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Retrofit retrofit;

    HelloServiceImpl(Retrofit retrofit) {
        this.retrofit = retrofit;
    }

    @Override
    public String hello(String name) throws TException {
        try {
            final HelloClient helloClient = retrofit.create(HelloClient.class);
            final String ret = helloClient.hello(name).get();
            log.debug("ret={}", ret);

            return "Hello, " + name;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
