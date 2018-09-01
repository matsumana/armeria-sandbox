package info.matsumana.armeria.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.linecorp.armeria.common.tracing.RequestContextCurrentTraceContext;

import brave.Tracing;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Sender;
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
                      .currentTraceContext(RequestContextCurrentTraceContext.DEFAULT)
                      .spanReporter(spanReporter())
                      .build();
    }

    /**
     * Configuration for how to buffer spans into messages for Zipkin
     */
    private AsyncReporter<Span> spanReporter() {
        final Sender sender = URLConnectionSender.create(endpoint);
        final AsyncReporter<Span> result = AsyncReporter.create(sender);
        // make sure spans are reported on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(result::close));
        return result;
    }
}
