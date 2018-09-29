package info.matsumana.armeria.handler;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.springframework.stereotype.Component;

import info.matsumana.armeria.thrift.FailService;

@Component
public class FailHandler implements FailService.AsyncIface {

    private boolean fail;

    @Override
    public void fail(AsyncMethodCallback<String> resultHandler) throws TException {
        fail = true;
        resultHandler.onComplete("failed");
    }

    @Override
    public void recover(AsyncMethodCallback<String> resultHandler) throws TException {
        fail = false;
        resultHandler.onComplete("recovered");
    }

    public boolean isFail() {
        return fail;
    }
}
