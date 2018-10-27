package info.matsumana.armeria.kubernetes.bean;

import java.io.Serializable;

/**
 * https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.12/#pod-v1-core
 */
public class Pod implements Serializable {

    private static final long serialVersionUID = 5026862294562969890L;

    private PodSpec spec;
    private PodStatus status;

    public PodSpec getSpec() {
        return spec;
    }

    public void setSpec(PodSpec spec) {
        this.spec = spec;
    }

    public PodStatus getStatus() {
        return status;
    }

    public void setStatus(PodStatus status) {
        this.status = status;
    }
}
