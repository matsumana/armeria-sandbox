package info.matsumana.armeria.retrofit;

import java.util.concurrent.CompletableFuture;

import retrofit2.http.GET;
import retrofit2.http.Path;

public interface HelloHttp {

    @GET("/hello/{name}")
    CompletableFuture<String> hello(@Path("name") String name);
}
