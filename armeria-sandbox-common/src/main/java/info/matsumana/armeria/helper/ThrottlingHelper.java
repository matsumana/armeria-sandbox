package info.matsumana.armeria.helper;

import static info.matsumana.armeria.helper.EndpointGroupHelper.CENTRAL_DOGMA_PROJECT;
import static info.matsumana.armeria.helper.EndpointGroupHelper.CENTRAL_DOGMA_REPOSITORY;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.completedFuture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.throttling.ThrottlingStrategy;
import com.linecorp.centraldogma.client.CentralDogma;
import com.linecorp.centraldogma.client.Watcher;
import com.linecorp.centraldogma.common.Query;

import info.matsumana.armeria.config.ThrottlingSetting;

@Component
public class ThrottlingHelper {

    private static final Logger log = LoggerFactory.getLogger(ThrottlingHelper.class);
    private static final ObjectReader reader = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, ThrottlingSetting>>() {});

    private final SecureRandom secureRandom = new SecureRandom();
    private Watcher<Map<String, ThrottlingSetting>> watcher;
    private Map<String, ThrottlingSetting> throttlingSetting = emptyMap();

    public ThrottlingHelper(@Nullable CentralDogma centralDogma) {
        if (centralDogma != null) {
            watchThrottlingSetting(centralDogma);
        }
    }

    public ThrottlingStrategy<HttpRequest> newThrottlingStrategy(String serviceName) {
        return new ThrottlingStrategy<>() {
            @Override
            public CompletionStage<Boolean> accept(ServiceRequestContext ctx, HttpRequest request) {
                final ThrottlingSetting setting = throttlingSetting.get(serviceName);
                final double ratio;
                if (setting == null) {
                    if (watcher == null || watcher.latestValue().get(serviceName) == null) {
                        ratio = 1;  // 100% acceptable
                    } else {
                        ratio = watcher.latestValue().get(serviceName).getRatio();
                    }
                } else {
                    ratio = throttlingSetting.get(serviceName).getRatio();
                }

                log.debug("Throttling ratio: name={}, ratio={}", serviceName, ratio);

                if (ratio < 1) {
                    return completedFuture(secureRandom.nextDouble() < ratio);
                } else {
                    return completedFuture(true);
                }
            }
        };
    }

    /**
     * throttling.json example
     * {
     *   "backend1": {
     *     "ratio": 1
     *   },
     *   "backend2": {
     *     "ratio": 1
     *   },
     *   "backend3": {
     *     "ratio": 1
     *   },
     *   "backend4": {
     *     "ratio": 1
     *   },
     *   "frontend": {
     *     "ratio": 1
     *   }
     * }
     */
    private void watchThrottlingSetting(CentralDogma centralDogma) {
        requireNonNull(centralDogma, "centralDogma");

        watcher = centralDogma.fileWatcher(CENTRAL_DOGMA_PROJECT, CENTRAL_DOGMA_REPOSITORY,
                                           Query.ofJsonPath("/throttling.json"),
                                           jsonNode -> {
                                               try {
                                                   return reader.readValue(jsonNode);
                                               } catch (IOException e) {
                                                   throw new UncheckedIOException(e);
                                               }
                                           });
        watcher.watch((revision, value) -> {
            log.debug("Updated throttling setting={}", value);
            throttlingSetting = value;
        });
    }
}
