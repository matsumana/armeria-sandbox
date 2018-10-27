package info.matsumana.armeria.bean.kubernetes;

import java.io.Serializable;
import java.util.List;

/**
 * https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.12/#container-v1-core
 */
public class Container implements Serializable {

    private static final long serialVersionUID = 3462160600267066856L;

    private List<ContainerPort> ports;

    public List<ContainerPort> getPorts() {
        return ports;
    }

    public void setPorts(List<ContainerPort> ports) {
        this.ports = ports;
    }
}
