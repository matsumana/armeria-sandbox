module info.matsumana.armeria.backend1 {
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

    requires brave;
    requires zipkin2;
    requires zipkin2.reporter;
    requires zipkin2.reporter.urlconnection;
}
