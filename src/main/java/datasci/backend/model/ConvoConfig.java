package datasci.backend.model;

import datasci.backend.activations.ActE;

public class ConvoConfig {

    public String actName = ActE.IDENT.label;
    // filter square matrix: filterSize rows, filterSize columns
    public int filterSize;
    // number of filter matrices
    public int numFilters;

    public ConvoConfig() {
    }


}  // end class
