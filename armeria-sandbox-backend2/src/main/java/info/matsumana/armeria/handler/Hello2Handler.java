package info.matsumana.armeria.handler;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.springframework.stereotype.Component;

import info.matsumana.armeria.thrift.Hello2Service;

@Component
public class Hello2Handler implements Hello2Service.AsyncIface {

    @Override
    public void hello(String name, AsyncMethodCallback<String> resultHandler) throws TException {
        resultHandler.onComplete("Hello, " + name);
    }
}
