package info.matsumana.armeria.thrift;

import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

@Component
public class PingServiceImpl implements PingService.Iface {

    @Override
    public String ping() throws TException {
        return "pong";
    }
}
