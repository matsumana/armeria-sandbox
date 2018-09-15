package info.matsumana.armeria.controller;

import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;

import info.matsumana.armeria.thrift.Hello1Service;
import info.matsumana.armeria.thrift.Hello2Service;
import info.matsumana.armeria.thrift.Hello3Service;

@Component
public class HelloController {

    private final Hello1Service.Iface hello1Service;
    private final Hello2Service.Iface hello2Service;
    private final Hello3Service.Iface hello3Service;

    HelloController(Hello1Service.Iface hello1Service,
                    Hello2Service.Iface hello2Service,
                    Hello3Service.Iface hello3Service) {
        this.hello1Service = hello1Service;
        this.hello2Service = hello2Service;
        this.hello3Service = hello3Service;
    }

    @Get("/hello/:name")
    public HttpResponse hello(@Param String name) throws TException {
        final String ret1 = hello1Service.hello(name);
        final String ret2 = hello2Service.hello(name);
        final String ret3 = hello3Service.hello(name);

        return HttpResponse.of("Hello, " + name);
    }
}
