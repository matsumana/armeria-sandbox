package info.matsumana.armeria.retrofit;

import static info.matsumana.armeria.task.PodInfoCollector.AUTHORIZATION_HEADER_KEY;

import java.util.concurrent.CompletableFuture;

import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface KubernetesClient {

    @GET("/api/v1/namespaces/{namespace}/pods")
    CompletableFuture<String> pods(@Header(AUTHORIZATION_HEADER_KEY) String authorization,
                                   @Path("namespace") String namespace,
                                   @Query("labelSelector") String labelSelector);
}
