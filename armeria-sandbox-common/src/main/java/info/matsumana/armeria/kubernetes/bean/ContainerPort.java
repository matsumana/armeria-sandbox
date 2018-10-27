package info.matsumana.armeria.kubernetes.bean;

import java.io.Serializable;

/**
 * https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.12/#containerport-v1-core
 */
public class ContainerPort implements Serializable {

    private static final long serialVersionUID = 2181440603377735893L;

    private int containerPort;

    public int getContainerPort() {
        return containerPort;
    }

    public void setContainerPort(int containerPort) {
        this.containerPort = containerPort;
    }
}
