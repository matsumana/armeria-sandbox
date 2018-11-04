package info.matsumana.armeria.config;

import java.io.Serializable;

public class ThrottlingSetting implements Serializable {

    private static final long serialVersionUID = -4121452215884296595L;

    private double ratio;

    public double getRatio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    @Override
    public String toString() {
        return "ThrottlingSetting{" +
               "ratio=" + ratio +
               '}';
    }
}
