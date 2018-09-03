package info.matsumana.armeria.thrift;

import java.util.concurrent.ExecutionException;

import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import com.linecorp.armeria.client.retrofit2.ArmeriaRetrofitBuilder;
import com.linecorp.armeria.client.tracing.HttpTracingClient;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;

import brave.Tracing;
import info.matsumana.armeria.config.ApiServerSetting;
import info.matsumana.armeria.config.ZipkinTracingFactory;
import info.matsumana.armeria.retrofit.HelloClient;
import retrofit2.Retrofit;
import retrofit2.adapter.java8.Java8CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Component
public class HelloServiceImpl implements HelloService.Iface {

    private final ApiServerSetting apiServerSetting;
    private final Tracing tracing;

    HelloServiceImpl(ApiServerSetting apiServerSetting, ZipkinTracingFactory tracingFactory) {
        this.apiServerSetting = apiServerSetting;
        tracing = tracingFactory.create("backend3");
    }

    @Override
    public String hello(String name) throws TException {
        {
            final Retrofit retrofit = new ArmeriaRetrofitBuilder()
                    .baseUrl(String.format("http://%s/", apiServerSetting.getBackend4()))
                    .addConverterFactory(ScalarsConverterFactory.create())
//                    .addConverterFactory(JacksonConverterFactory.create())
                    .addCallAdapterFactory(Java8CallAdapterFactory.create())
                    .withClientOptions((uri, optionsBuilder) ->
                                               optionsBuilder.decorator(
                                                       HttpRequest.class, HttpResponse.class,
                                                       HttpTracingClient.newDecorator(tracing, "backend4")))
                    .build();

            try {
                final HelloClient helloClient = retrofit.create(HelloClient.class);
                final String ret = helloClient.hello(name).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        return "Hello, " + name;
    }
}
