package info.matsumana.armeria.kubernetes.bean;

import java.io.Serializable;

public class Port implements Serializable {

    private static final long serialVersionUID = 2181440603377735893L;

    private int containerPort;

    public int getContainerPort() {
        return containerPort;
    }

    public void setContainerPort(int containerPort) {
        this.containerPort = containerPort;
    }
}
