package info.matsumana.armeria.handler;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.springframework.stereotype.Component;

import info.matsumana.armeria.thrift.PingService;

@Component
public class PingHandler implements PingService.AsyncIface {

    @Override
    public void ping(AsyncMethodCallback<String> resultHandler) throws TException {
        resultHandler.onComplete("pong");
    }
}
