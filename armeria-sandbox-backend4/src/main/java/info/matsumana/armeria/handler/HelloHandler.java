package info.matsumana.armeria.handler;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.common.util.SystemInfo;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;

import info.matsumana.armeria.bean.handler.HelloResponse;

@Component
public class HelloHandler {

    private static final ObjectWriter objectWriter = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .writerFor(new TypeReference<HelloResponse>() {});

    @Get("/hello/:name")
    public HttpResponse hello(@Param String name) throws JsonProcessingException {
        final HelloResponse response = new HelloResponse();
        response.setServerName(SystemInfo.hostname());
        response.setMessage("Hello, " + name);

        final String json = objectWriter.writeValueAsString(response);
        return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, json);
    }
}
