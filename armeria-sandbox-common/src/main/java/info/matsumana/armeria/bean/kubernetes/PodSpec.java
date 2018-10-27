package info.matsumana.armeria.bean.kubernetes;

import java.io.Serializable;
import java.util.List;

/**
 * https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.12/#podspec-v1-core
 */
public class PodSpec implements Serializable {

    private static final long serialVersionUID = -8830069108680404673L;

    private List<Container> containers;

    public List<Container> getContainers() {
        return containers;
    }

    public void setContainers(List<Container> containers) {
        this.containers = containers;
    }
}
