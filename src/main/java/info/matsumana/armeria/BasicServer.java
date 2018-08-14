package info.matsumana.armeria;

import java.util.concurrent.CompletableFuture;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;

public final class BasicServer {

    public static void main(String[] args) {
        final ServerBuilder sb = new ServerBuilder();
        sb.http(8080);

        sb.service("/hello", (ctx, res) -> HttpResponse.of(
                HttpStatus.OK, MediaType.PLAIN_TEXT_UTF_8, "Hello World!"));

        final Server server = sb.build();
        final CompletableFuture<Void> future = server.start();
        future.join();
    }
}
