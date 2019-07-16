package info.matsumana.armeria.handler;

import org.springframework.stereotype.Component;

import com.linecorp.armeria.common.util.SystemInfo;

import info.matsumana.armeria.grpc.Hello2.Hello2Request;
import info.matsumana.armeria.grpc.Hello2.Hello2Response;
import info.matsumana.armeria.grpc.Hello2ServiceGrpc.Hello2ServiceImplBase;
import io.grpc.stub.StreamObserver;

@Component
public class Hello2Handler extends Hello2ServiceImplBase {

    @Override
    public void hello(Hello2Request req, StreamObserver<Hello2Response> responseObserver) {
        final Hello2Response response = Hello2Response.newBuilder()
                                                      .setServerName(SystemInfo.hostname())
                                                      .setMessage("Hello, " + req.getName())
                                                      .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
