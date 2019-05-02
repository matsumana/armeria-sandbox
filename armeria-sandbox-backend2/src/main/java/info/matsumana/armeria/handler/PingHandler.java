package info.matsumana.armeria.handler;

import org.springframework.stereotype.Component;

import info.matsumana.armeria.grpc.Ping.PingReply;
import info.matsumana.armeria.grpc.Ping.PingRequest;
import info.matsumana.armeria.grpc.PingServiceGrpc.PingServiceImplBase;
import io.grpc.stub.StreamObserver;

@Component
public class PingHandler extends PingServiceImplBase {

    @Override
    public void ping(PingRequest req, StreamObserver<PingReply> responseObserver) {
        final PingReply reply = PingReply.newBuilder()
                                         .setMessage("pong")
                                         .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
