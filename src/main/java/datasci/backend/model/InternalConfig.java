package datasci.backend.model;

import datasci.backend.activations.ActE;

public class InternalConfig {
    public String actName = ActE.LEAKY_RELU.label;
    public int numOutputNodes;

    public InternalConfig() {
    }

}  //end class
