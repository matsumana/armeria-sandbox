package info.matsumana.armeria.kubernetes.bean;

import java.io.Serializable;
import java.util.List;

public class PodList implements Serializable {

    private static final long serialVersionUID = -1054365497517060685L;

    private List<Pod> items;

    public List<Pod> getItems() {
        return items;
    }

    public void setItems(List<Pod> items) {
        this.items = items;
    }
}
