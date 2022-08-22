package datasci.backend.model;

import datasci.backend.activations.ActE;

public class PoolConfig {
    public String actName = ActE.NONE.label;
    // pool square matrix: poolSize rows, poolSize columns
    public int poolSize;

    public PoolConfig() {
    }

}  //end class
