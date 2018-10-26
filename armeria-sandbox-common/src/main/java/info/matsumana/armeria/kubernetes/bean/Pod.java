package info.matsumana.armeria.kubernetes.bean;

import java.io.Serializable;

public class Pod implements Serializable {

    private static final long serialVersionUID = 5026862294562969890L;

    private Spec spec;
    private Status status;

    public Spec getSpec() {
        return spec;
    }

    public void setSpec(Spec spec) {
        this.spec = spec;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
