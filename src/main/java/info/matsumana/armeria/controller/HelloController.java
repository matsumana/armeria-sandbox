package info.matsumana.armeria.controller;

import org.apache.thrift.TException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linecorp.armeria.client.Clients;

import info.matsumana.armeria.config.ApiServerConfig;
import info.matsumana.armeria.thrift.HelloService;

@RestController
@RequestMapping("/")
public class HelloController {

    private final ApiServerConfig apiServerConfig;

    HelloController(ApiServerConfig apiServerConfig) {
        this.apiServerConfig = apiServerConfig;
    }

    @GetMapping("hello")
    String hello() {
        return "Hello, World";
    }

    @GetMapping("hello/{name}")
    String hello(@PathVariable String name) throws TException {
        final HelloService.Iface helloService = Clients.newClient(
                String.format("tbinary+http://%s/thrift/hello", apiServerConfig.getThriftServer()),
                HelloService.Iface.class);
        return helloService.hello(name);
    }
}
