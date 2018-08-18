package info.matsumana.armeria.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;

import com.linecorp.armeria.server.healthcheck.HealthChecker;
import com.linecorp.armeria.server.thrift.THttpService;
import com.linecorp.armeria.server.tomcat.TomcatService;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;
import com.linecorp.armeria.spring.ThriftServiceRegistrationBean;

import info.matsumana.armeria.thrift.HelloService;

@Configuration
public class ArmeriaConfig {

    private static Connector getConnector(ServletWebServerApplicationContext applicationContext) {
        final TomcatWebServer container = (TomcatWebServer) applicationContext.getWebServer();
        container.start();
        return container.getTomcat().getConnector();
    }

    @Bean
    public HealthChecker tomcatConnectorHealthChecker(ServletWebServerApplicationContext applicationContext) {
        final Connector connector = getConnector(applicationContext);
        return () -> connector.getState().isAvailable();
    }

    @Bean
    public TomcatService tomcatService(ServletWebServerApplicationContext applicationContext) {
        return TomcatService.forConnector(getConnector(applicationContext));
    }

    @Bean
    public ArmeriaServerConfigurator armeriaServiceInitializer(TomcatService tomcatService) {
        return sb -> sb.service("prefix:/", tomcatService);
    }

    @Bean
    public ThriftServiceRegistrationBean helloService(HelloService.Iface helloService) {
        return new ThriftServiceRegistrationBean()
                .setPath("/thrift/hello")
                .setService(THttpService.of(helloService))
                .setServiceName("HelloService")
                .setExampleRequests(ImmutableList.of(new HelloService.hello_args("foo")));
    }
}
