package info.matsumana.armeria.task;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.linecorp.centraldogma.client.CentralDogma;
import com.linecorp.centraldogma.common.Change;
import com.linecorp.centraldogma.common.Revision;

import info.matsumana.armeria.bean.kubernetes.PodList;
import info.matsumana.armeria.helper.EndpointGroupHelper;
import info.matsumana.armeria.helper.KubernetesModelHelper;
import info.matsumana.armeria.retrofit.KubernetesClient;
import retrofit2.Retrofit;

@Component
public class PodInfoCollector {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final String AUTHORIZATION_HEADER_KEY = "Authorization";
    public static final String AUTHORIZATION_HEADER_VALUE = "Bearer %s";
    private static final String LABEL_SELECTOR = "app=armeria-sandbox-%s";

    private final String namespace;
    private final String token;
    private final CentralDogma centralDogma;
    private final KubernetesClient client;

    public PodInfoCollector(@Value("${kubernetes.namespace}") String namespace,
                            @Value("${kubernetes.token:}") String token,
                            CentralDogma centralDogma, Retrofit retrofit) {
        this.namespace = namespace;
        this.token = token;
        this.centralDogma = centralDogma;
        client = retrofit.create(KubernetesClient.class);
    }

    @Scheduled(initialDelay = 10_000, fixedDelay = 5_000)
    void updatePodIp() {
        Arrays.asList("backend1", "backend2", "backend3", "backend4", "frontend")
              .forEach(this::saveToCentralDogma);
    }

    private void saveToCentralDogma(String app) {
        final String json = getPodInfo(app);

        log.debug("json = {}", json);

        // Since resourceVersion which is included Kubernetes api response will be changed every request,
        // deserialize and serialize it to remove it from json.
        // Then save it to Central Dogma.
        final PodList podList = KubernetesModelHelper.deserializePodList(json);
        final String treeShakedJson = KubernetesModelHelper.serializePodList(podList);

        log.debug("treeShakedJson = {}", treeShakedJson);

        centralDogma.push(EndpointGroupHelper.CENTRAL_DOGMA_PROJECT,
                          EndpointGroupHelper.CENTRAL_DOGMA_REPOSITORY,
                          Revision.HEAD,
                          String.format("Updated by %s", getClass().getName()),
                          Change.ofTextUpsert(String.format("/%s.json", app), treeShakedJson));

    }

    private String getPodInfo(String app) {
        return client.pods(namespace,
                           String.format(AUTHORIZATION_HEADER_VALUE, token),
                           String.format(LABEL_SELECTOR, app))
                     .join();
    }
}
