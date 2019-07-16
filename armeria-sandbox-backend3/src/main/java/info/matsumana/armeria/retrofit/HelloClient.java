package info.matsumana.armeria.retrofit;

import java.util.concurrent.CompletableFuture;

import info.matsumana.armeria.thrift.Hello4Response;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface HelloClient {

    @GET("/hello/{name}")
    CompletableFuture<Hello4Response> hello(@Path("name") String name);
}
