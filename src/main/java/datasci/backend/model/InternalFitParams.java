package datasci.backend.model;

public class InternalFitParams {
    // fit parameters for one internal layer
    public String layerID;
    // weight matrix: nOut rows, nIn columns
    public Matrix w;
    // b matrix: 1 col, nOut rows
    public Matrix b;


    public InternalFitParams() {
    }
}
