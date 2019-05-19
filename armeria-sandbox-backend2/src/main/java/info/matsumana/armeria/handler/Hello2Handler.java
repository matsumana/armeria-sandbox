package info.matsumana.armeria.handler;

import org.springframework.stereotype.Component;

import info.matsumana.armeria.grpc.Hello2.Hello2Reply;
import info.matsumana.armeria.grpc.Hello2.Hello2Request;
import info.matsumana.armeria.grpc.Hello2ServiceGrpc.Hello2ServiceImplBase;
import io.grpc.stub.StreamObserver;

@Component
public class Hello2Handler extends Hello2ServiceImplBase {

    @Override
    public void hello(Hello2Request req, StreamObserver<Hello2Reply> responseObserver) {
        final Hello2Reply reply = Hello2Reply.newBuilder()
                                             .setMessage("[backend2] Hello, " + req.getName())
                                             .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
