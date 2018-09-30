module info.matsumana.armeria.frontend {
    exports info.matsumana.armeria;

    requires info.matsumana.armeria.thrift;
    requires info.matsumana.armeria.common;

    requires spring.context;
    requires spring.beans;
    requires spring.boot;

    requires armeria;
    requires armeria.spring.boot.autoconfigure;
    requires armeria.thrift;
    requires armeria.zipkin;

    requires libthrift;

    requires com.google.common;
    requires io.reactivex.rxjava2;
    requires rxjava2.jdk8.interop;
    requires slf4j.api;

    requires brave;
    requires zipkin2;
    requires zipkin2.reporter;
    requires zipkin2.reporter.urlconnection;
    requires micrometer.core;
}
