package info.matsumana.armeria.handler;

import org.springframework.stereotype.Component;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;

@Component
public class HelloHandler {

    @Get("/hello/:name")
    public HttpResponse hello(@Param String name) {
        return HttpResponse.of("[backend4] Hello, " + name);
    }
}
