package info.matsumana.armeria.handler;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.springframework.stereotype.Component;

import info.matsumana.armeria.thrift.Hello1Service;

@Component
public class Hello1Handler implements Hello1Service.AsyncIface {

    @Override
    public void hello(String name, AsyncMethodCallback<String> resultHandler) throws TException {
        resultHandler.onComplete("[backend1] Hello, " + name);
    }
}
