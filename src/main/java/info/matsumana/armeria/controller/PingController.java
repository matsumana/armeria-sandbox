package info.matsumana.armeria.controller;

import org.apache.thrift.TException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linecorp.armeria.client.Clients;

import info.matsumana.armeria.config.ApiServerConfig;
import info.matsumana.armeria.thrift.PingService;

@RestController
@RequestMapping("/")
public class PingController {

    private final ApiServerConfig apiServerConfig;

    PingController(ApiServerConfig apiServerConfig) {
        this.apiServerConfig = apiServerConfig;
    }

    @GetMapping("ping")
    String ping() throws TException {
        final PingService.Iface pingService = Clients.newClient(
                String.format("tbinary+http://%s/thrift/ping", apiServerConfig.getThriftServer()),
                PingService.Iface.class);
        return pingService.ping();
    }
}
