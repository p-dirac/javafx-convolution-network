package datasci.backend.model;

public class OutputFitParams {

    // weight matrix: nOut rows, nIn columns
    public Matrix w;
    // bias matrix: 1 col, nOut rows
    public Matrix b;

    public OutputFitParams() {
    }
}
