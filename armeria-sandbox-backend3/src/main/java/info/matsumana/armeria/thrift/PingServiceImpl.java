package info.matsumana.armeria.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.springframework.stereotype.Component;

@Component
public class PingServiceImpl implements PingService.AsyncIface {

    @Override
    public void ping(AsyncMethodCallback<String> resultHandler) throws TException {
        resultHandler.onComplete("pong");
    }
}
