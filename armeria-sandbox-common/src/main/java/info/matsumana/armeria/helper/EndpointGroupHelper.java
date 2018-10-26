package info.matsumana.armeria.helper;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import com.linecorp.armeria.client.Endpoint;
import com.linecorp.armeria.client.endpoint.EndpointGroup;
import com.linecorp.armeria.client.endpoint.StaticEndpointGroup;
import com.linecorp.centraldogma.client.CentralDogma;
import com.linecorp.centraldogma.client.Watcher;
import com.linecorp.centraldogma.client.armeria.CentralDogmaEndpointGroup;
import com.linecorp.centraldogma.common.Query;

import info.matsumana.armeria.config.ApiServerSetting.EndpointSetting;
import info.matsumana.armeria.kubernetes.bean.Container;
import info.matsumana.armeria.kubernetes.bean.Pod;
import info.matsumana.armeria.kubernetes.bean.PodList;
import info.matsumana.armeria.kubernetes.bean.Port;

@Component
public class EndpointGroupHelper {

    private static final String CENTRAL_DOGMA_PROJECT = "armeriaSandbox";
    private static final String CENTRAL_DOGMA_REPOSITORY = "apiServers";
    private static final int JMX_PORT = 8686;

    private final CentralDogma centralDogma;

    private final ObjectReader objectReader = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .readerFor(new TypeReference<PodList>() {});

    public EndpointGroupHelper(@Nullable CentralDogma centralDogma) {
        this.centralDogma = centralDogma;
    }

    public EndpointGroup newEndpointGroup(String path, List<EndpointSetting> staticList) {
        if (centralDogma == null) {
            return newStaticEndpointGroup(staticList);
        } else {
            return newCentralDogmaEndpointGroup(path);
        }
    }

    private EndpointGroup newCentralDogmaEndpointGroup(String path) {
        final Watcher<List<Endpoint>> watcher =
                Objects.requireNonNull(centralDogma)
                       .fileWatcher(CENTRAL_DOGMA_PROJECT, CENTRAL_DOGMA_REPOSITORY, Query.ofJsonPath(path),
                                    jsonNode -> {
                                        try {
                                            return objectReader.<PodList>readValue(jsonNode.toString())
                                                    .getItems().stream()
                                                    .map(toEndpoint())
                                                    .collect(toUnmodifiableList());
                                        } catch (IOException e) {
                                            throw new UncheckedIOException(e);
                                        }
                                    });

        final CentralDogmaEndpointGroup<List<Endpoint>> group = CentralDogmaEndpointGroup
                .ofWatcher(watcher, list -> list);

        try {
            group.awaitInitialEndpoints(30, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        return group;
    }

    private static Function<Pod, Endpoint> toEndpoint() {
        return pod -> {
            final String podIp = pod.getStatus().getPodIP();
            final int port = pod.getSpec().getContainers().stream()
                                .findFirst()    // I expect 1 pod includes only 1 container
                                .map(toPort())
                                .orElseThrow(() -> new RuntimeException("container not found"));
            return Endpoint.of(podIp, port);
        };
    }

    private static Function<Container, Integer> toPort() {
        return container -> container.getPorts().stream()
                                     .filter(port -> port.getContainerPort() != JMX_PORT)
                                     .map(Port::getContainerPort)
                                     .findFirst()  // I expect 1 pod has only 1 port except JMX port
                                     .orElseThrow(() -> new RuntimeException("port not found"));
    }

    private static EndpointGroup newStaticEndpointGroup(List<EndpointSetting> staticList) {
        return new StaticEndpointGroup(staticList.stream()
                                                 .map(setting -> Endpoint.of(setting.getHost(),
                                                                             setting.getPort()))
                                                 .collect(toUnmodifiableList()));
    }
}
