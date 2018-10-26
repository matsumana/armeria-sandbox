package info.matsumana.armeria.kubernetes.bean;

import java.io.Serializable;
import java.util.List;

public class Spec implements Serializable {

    private static final long serialVersionUID = -8830069108680404673L;

    private List<Container> containers;

    public List<Container> getContainers() {
        return containers;
    }

    public void setContainers(List<Container> containers) {
        this.containers = containers;
    }
}
