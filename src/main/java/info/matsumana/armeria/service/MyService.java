package info.matsumana.armeria.service;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;

public class MyService {

    @Get("/service")
    public HttpResponse service() {
        return HttpResponse.of(
                HttpStatus.OK, MediaType.PLAIN_TEXT_UTF_8, "My Service");
    }

    @Get("/greet/:name")
    public HttpResponse greet(@Param String name) {
        return HttpResponse.of(
                HttpStatus.OK, MediaType.PLAIN_TEXT_UTF_8, "Hello " + name);
    }
}
