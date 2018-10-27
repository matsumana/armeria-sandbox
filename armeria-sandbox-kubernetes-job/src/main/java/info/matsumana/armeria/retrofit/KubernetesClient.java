package info.matsumana.armeria.retrofit;

import java.util.concurrent.CompletableFuture;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface KubernetesClient {

    @GET("/api/v1/namespaces/{namespace}/pods")
    CompletableFuture<String> pods(@Path("namespace") String namespace,
                                   @Query("labelSelector") String labelSelector);
}
