package info.matsumana.armeria.kubernetes.bean;

import java.io.Serializable;
import java.util.List;

/**
 * https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.12/#podlist-v1-core
 */
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
