package info.matsumana.armeria.helper;

import java.io.IOException;
import java.io.UncheckedIOException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import info.matsumana.armeria.bean.kubernetes.PodList;

public class KubernetesModelHelper {

    private KubernetesModelHelper() {
    }

    private static final ObjectReader podListReader = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .readerFor(new TypeReference<PodList>() {});

    private static final ObjectWriter podListWriter = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .writerFor(new TypeReference<PodList>() {});

    public static String serializePodList(PodList podList) {
        try {
            return podListWriter.writeValueAsString(podList);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static PodList deserializePodList(JsonNode jsonNode) {
        try {
            return podListReader.readValue(jsonNode);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static PodList deserializePodList(String jsonNode) {
        try {
            return podListReader.readValue(jsonNode);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
