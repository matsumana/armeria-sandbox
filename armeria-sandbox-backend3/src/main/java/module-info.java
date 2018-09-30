module info.matsumana.armeria.backend3 {
    exports info.matsumana.armeria;

    requires info.matsumana.armeria.thrift;
    requires info.matsumana.armeria.common;

    requires spring.context;
    requires spring.beans;
    requires spring.boot;

    requires armeria;
    requires armeria.spring.boot.autoconfigure;
    requires armeria.thrift;
    requires armeria.retrofit2;
    requires armeria.zipkin;

    requires libthrift;

    requires retrofit2;
    requires retrofit2.adapter.java8;
    requires retrofit2.converter.scalars;

    requires brave;
    requires zipkin2;
    requires zipkin2.reporter;
    requires zipkin2.reporter.urlconnection;
    requires micrometer.core;
}
