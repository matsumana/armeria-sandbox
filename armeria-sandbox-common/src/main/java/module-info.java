module info.matsumana.armeria.common {
    exports info.matsumana.armeria.common.config;

    requires spring.context;
    requires spring.beans;
    requires spring.boot;

    requires armeria.zipkin;

    requires brave;
    requires zipkin2;
    requires zipkin2.reporter;
    requires zipkin2.reporter.urlconnection;
}
