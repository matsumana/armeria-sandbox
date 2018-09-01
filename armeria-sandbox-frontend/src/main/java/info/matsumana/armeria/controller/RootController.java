package info.matsumana.armeria.controller;

import org.springframework.stereotype.Component;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.annotation.Get;

@Component
public class RootController {

    @Get("/")
    public HttpResponse index() {
        return HttpResponse.of("index");
    }
}
