package info.matsumana.armeria.kubernetes.bean;

import java.io.Serializable;
import java.util.List;

public class Container implements Serializable {

    private static final long serialVersionUID = 3462160600267066856L;

    private List<Port> ports;

    public List<Port> getPorts() {
        return ports;
    }

    public void setPorts(List<Port> ports) {
        this.ports = ports;
    }
}
