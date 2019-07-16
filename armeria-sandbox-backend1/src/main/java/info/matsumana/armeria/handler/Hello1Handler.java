package info.matsumana.armeria.handler;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.springframework.stereotype.Component;

import com.linecorp.armeria.common.util.SystemInfo;

import info.matsumana.armeria.thrift.Hello1Response;
import info.matsumana.armeria.thrift.Hello1Service;

@Component
public class Hello1Handler implements Hello1Service.AsyncIface {

    @Override
    public void hello(String name, AsyncMethodCallback<Hello1Response> resultHandler) throws TException {
        resultHandler.onComplete(new Hello1Response(SystemInfo.hostname(), "Hello, " + name));
    }
}
