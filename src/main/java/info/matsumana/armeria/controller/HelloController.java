package info.matsumana.armeria.controller;

import org.apache.thrift.TException;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linecorp.armeria.client.Clients;
import com.linecorp.armeria.server.Server;

import info.matsumana.armeria.thrift.HelloService;

@RestController
@RequestMapping("/")
public class HelloController {

    private String thriftServer;

    HelloController(@Nullable Server server) {
        if (server != null) {
            thriftServer = "localhost:" + server.activePort().get().localAddress().getPort();
        }
    }

    @GetMapping("hello")
    String hello() {
        return "Hello, World";
    }

    @GetMapping("hello/{name}")
    String hello(@PathVariable String name) throws TException {
        final HelloService.Iface helloService = Clients.newClient(
                String.format("tbinary+http://%s/thrift/hello", thriftServer),
                HelloService.Iface.class);
        return helloService.hello(name);
    }
}
