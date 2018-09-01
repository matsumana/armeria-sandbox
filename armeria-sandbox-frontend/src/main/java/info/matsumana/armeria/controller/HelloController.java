package info.matsumana.armeria.controller;

import org.apache.thrift.TException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linecorp.armeria.client.ClientBuilder;
import com.linecorp.armeria.client.tracing.HttpTracingClient;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;

import brave.Tracing;
import info.matsumana.armeria.config.ApiServerSetting;
import info.matsumana.armeria.config.ZipkinTracingFactory;
import info.matsumana.armeria.thrift.HelloService;

@RestController
@RequestMapping("/")
public class HelloController {

    private final ApiServerSetting apiServerSetting;
    private final Tracing tracing;

    HelloController(ApiServerSetting apiServerSetting, ZipkinTracingFactory tracingFactory) {
        this.apiServerSetting = apiServerSetting;
        tracing = tracingFactory.create("frontend");
    }

    @GetMapping("hello/{name}")
    String hello(@PathVariable String name) throws TException {
        {
            final HelloService.Iface helloService = new ClientBuilder(
                    String.format("tbinary+h2c://%s/thrift/hello", apiServerSetting.getBackend1()))
                    .decorator(HttpRequest.class, HttpResponse.class,
                               HttpTracingClient.newDecorator(tracing, "backend1"))
                    .build(HelloService.Iface.class);
            final String ret1 = helloService.hello(name);
        }

        {
            final HelloService.Iface helloService = new ClientBuilder(
                    String.format("tbinary+h2c://%s/thrift/hello", apiServerSetting.getBackend2()))
                    .decorator(HttpRequest.class, HttpResponse.class,
                               HttpTracingClient.newDecorator(tracing, "backend2"))
                    .build(HelloService.Iface.class);
            final String ret2 = helloService.hello(name);
        }

        {
            final HelloService.Iface helloService = new ClientBuilder(
                    String.format("tbinary+h2c://%s/thrift/hello", apiServerSetting.getBackend3()))
                    .decorator(HttpRequest.class, HttpResponse.class,
                               HttpTracingClient.newDecorator(tracing, "backend3"))
                    .build(HelloService.Iface.class);
            final String ret3 = helloService.hello(name);
        }

        return "Hello, " + name;
    }
}
