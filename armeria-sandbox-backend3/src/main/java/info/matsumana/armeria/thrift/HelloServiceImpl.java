package info.matsumana.armeria.thrift;

import java.util.concurrent.ExecutionException;

import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import info.matsumana.armeria.retrofit.HelloClient;
import retrofit2.Retrofit;

@Component
public class HelloServiceImpl implements Hello3Service.Iface {

    private final Retrofit retrofit;

    HelloServiceImpl(Retrofit retrofit) {
        this.retrofit = retrofit;
    }

    @Override
    public String hello(String name) throws TException {
        try {
            final HelloClient helloClient = retrofit.create(HelloClient.class);
            final String ret = helloClient.hello(name).get();

            return "Hello, " + name;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
