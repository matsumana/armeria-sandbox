module info.matsumana.armeria.backend4 {
    exports info.matsumana.armeria;

    requires info.matsumana.armeria.common;

    requires spring.context;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;

    requires armeria;
    requires armeria.spring.boot.autoconfigure;
    requires armeria.zipkin;

    requires brave;
    requires zipkin2;
    requires zipkin2.reporter;
    requires zipkin2.reporter.urlconnection;
}
