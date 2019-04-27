package info.matsumana.armeria.handler;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.springframework.stereotype.Component;

import info.matsumana.armeria.thrift.Hello1Service;

@Component
public class Hello1Handler implements Hello1Service.AsyncIface {

    private final FailHandler failHandler;

    Hello1Handler(FailHandler failHandler) {
        this.failHandler = failHandler;
    }

    @Override
    public void hello(String name, AsyncMethodCallback<String> resultHandler) throws TException {
        // Just test for Circuit Breaker
        testCircuitBreaker();

        resultHandler.onComplete("[backend1] Hello, " + name);
    }

    private void testCircuitBreaker() {
        if (failHandler.isFail()) {
            throw new RuntimeException("Circuit breaker test");
        }
    }
}
