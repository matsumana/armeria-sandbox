package info.matsumana.armeria.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.springframework.stereotype.Component;

@Component
public class HelloServiceImpl implements Hello2Service.AsyncIface {

    @Override
    public void hello(String name, AsyncMethodCallback<String> resultHandler) throws TException {
        resultHandler.onComplete("Hello, " + name);
    }
}
