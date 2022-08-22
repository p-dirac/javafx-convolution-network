package datasci.backend.model;

import datasci.backend.activations.ActE;

public class OutputConfig {

    public String actName = ActE.SOFTMAX.label;
    public int numOutputNodes;


    public OutputConfig() {
    }

}  //end class
