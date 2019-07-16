package info.matsumana.armeria.bean;

import java.io.Serializable;

import info.matsumana.armeria.bean.handler.HelloResponse;

public class FrontendResponse implements Serializable {

    private static final long serialVersionUID = 972559530665843432L;

    private HelloResponse backend1;
    private HelloResponse backend2;
    private HelloResponse backend3;
    private HelloResponse backend4;

    public HelloResponse getBackend1() {
        return backend1;
    }

    public void setBackend1(HelloResponse backend1) {
        this.backend1 = backend1;
    }

    public HelloResponse getBackend2() {
        return backend2;
    }

    public void setBackend2(HelloResponse backend2) {
        this.backend2 = backend2;
    }

    public HelloResponse getBackend3() {
        return backend3;
    }

    public void setBackend3(HelloResponse backend3) {
        this.backend3 = backend3;
    }

    public HelloResponse getBackend4() {
        return backend4;
    }

    public void setBackend4(HelloResponse backend4) {
        this.backend4 = backend4;
    }
}
