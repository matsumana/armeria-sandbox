package info.matsumana.armeria.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.linecorp.armeria.common.brave.RequestContextCurrentTraceContext;

import brave.Tracing;
import zipkin2.reporter.Sender;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.urlconnection.URLConnectionSender;

@Component
public class ZipkinTracingFactory {

    private final String endpoint;

    ZipkinTracingFactory(@Value("${zipkin.endpoint}") String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Controls aspects of tracing such as the name that shows up in the
     */
    public Tracing create(String serviceName) {
        return Tracing.newBuilder()
                      .localServiceName(serviceName)
                      .currentTraceContext(RequestContextCurrentTraceContext.ofDefault())
                      .addSpanHandler(spanHandler())
                      .build();
    }

    /**
     * Configuration for how to buffer spans into messages for Zipkin
     */
    private AsyncZipkinSpanHandler spanHandler() {
        final Sender sender = URLConnectionSender.create(endpoint);
        return AsyncZipkinSpanHandler.create(sender);
    }
}
