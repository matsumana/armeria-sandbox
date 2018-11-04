package info.matsumana.armeria.helper;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.linecorp.armeria.client.Endpoint;
import com.linecorp.armeria.client.endpoint.EndpointGroup;
import com.linecorp.armeria.client.endpoint.StaticEndpointGroup;
import com.linecorp.centraldogma.client.CentralDogma;
import com.linecorp.centraldogma.client.Watcher;
import com.linecorp.centraldogma.client.armeria.CentralDogmaEndpointGroup;
import com.linecorp.centraldogma.common.Query;

import info.matsumana.armeria.bean.kubernetes.Container;
import info.matsumana.armeria.bean.kubernetes.ContainerPort;
import info.matsumana.armeria.bean.kubernetes.Pod;
import info.matsumana.armeria.config.ApiServerSetting.EndpointSetting;

@Component
public class EndpointGroupHelper {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final String CENTRAL_DOGMA_PROJECT = "armeriaSandbox";
    public static final String CENTRAL_DOGMA_REPOSITORY = "apiServers";
    private static final int JMX_PORT = 8686;

    private final CentralDogma centralDogma;

    public EndpointGroupHelper(@Nullable CentralDogma centralDogma) {
        this.centralDogma = centralDogma;
    }

    public EndpointGroup newEndpointGroup(String centralDogmaFile, List<EndpointSetting> staticList) {
        if (centralDogma == null) {
            return newStaticEndpointGroup(staticList);
        } else {
            return newCentralDogmaEndpointGroup(centralDogmaFile);
        }
    }

    private EndpointGroup newCentralDogmaEndpointGroup(String centralDogmaFile) {
        requireNonNull(centralDogma, "centralDogma");

        final Watcher<List<Endpoint>> watcher = centralDogma
                .fileWatcher(CENTRAL_DOGMA_PROJECT, CENTRAL_DOGMA_REPOSITORY,
                             Query.ofJsonPath(centralDogmaFile),
                             jsonNode -> {
                                 final List<Endpoint> endpoints =
                                         KubernetesModelHelper.deserializePodList(jsonNode)
                                                              .getItems().stream()
                                                              .filter(pod -> "Running".equals(pod.getStatus()
                                                                                                 .getPhase()))
                                                              .map(toEndpoint())
                                                              .collect(toUnmodifiableList());

                                 log.info("centralDogmaFile = {}, endpoints = {}",
                                          centralDogmaFile, endpoints);

                                 return endpoints;
                             });

        final CentralDogmaEndpointGroup<List<Endpoint>> group = CentralDogmaEndpointGroup
                .ofWatcher(watcher, endpoints -> endpoints);

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
                                     .map(ContainerPort::getContainerPort)
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
