package datasci.backend.model;

public class InternalFitParams {

    // weight matrix: nOut rows, nIn columns
    public Matrix w;
    // b matrix: 1 col, nOut rows
    public Matrix b;


    public InternalFitParams() {
    }
}
