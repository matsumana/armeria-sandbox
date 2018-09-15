package info.matsumana.armeria.thrift;

import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

@Component
public class HelloServiceImpl implements Hello1Service.Iface {

    @Override
    public String hello(String name) throws TException {
        return "Hello, " + name;
    }
}
