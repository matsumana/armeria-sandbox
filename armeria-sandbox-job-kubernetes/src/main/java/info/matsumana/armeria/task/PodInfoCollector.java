package info.matsumana.armeria.task;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import com.linecorp.centraldogma.client.CentralDogma;
import com.linecorp.centraldogma.common.Change;
import com.linecorp.centraldogma.common.Revision;

import info.matsumana.armeria.bean.kubernetes.PodList;
import info.matsumana.armeria.helper.EndpointGroupHelper;
import info.matsumana.armeria.retrofit.KubernetesClient;
import retrofit2.Retrofit;

@Component
public class PodInfoCollector {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final ObjectWriter podListWriter = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .writerFor(new TypeReference<PodList>() {});

    public static final String AUTHORIZATION_HEADER_KEY = "Authorization";
    public static final String AUTHORIZATION_HEADER_VALUE = "Bearer %s";
    private static final String LABEL_SELECTOR = "deployment=armeria-sandbox-%s";

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

    private void saveToCentralDogma(String deployment) {
        final PodList podList = getPodInfo(deployment);
        final String json = serializePodList(podList);

        log.debug("pods = {}", json);

        centralDogma.push(EndpointGroupHelper.CENTRAL_DOGMA_PROJECT,
                          EndpointGroupHelper.CENTRAL_DOGMA_REPOSITORY,
                          Revision.HEAD,
                          String.format("Updated by %s", getClass().getName()),
                          Change.ofTextUpsert(String.format("/%s.json", deployment), json));
    }

    private PodList getPodInfo(String deployment) {
        return client.pods(String.format(AUTHORIZATION_HEADER_VALUE, token),
                           namespace,
                           String.format(LABEL_SELECTOR, deployment))
                     .join();
    }

    private static String serializePodList(PodList podList) {
        try {
            return podListWriter.writeValueAsString(podList);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
