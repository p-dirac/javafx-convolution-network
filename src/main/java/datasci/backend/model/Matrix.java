package datasci.backend.model;

import datasci.backend.activations.LeakyReluActivation;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Matrix in one-D row format
 */
public class Matrix {
    private static final Logger LOG = Logger.getLogger(LeakyReluActivation.class.getName());
    //
    public int rows;
    public int cols;
    public int size;
    public double[] a;

    /**
     * Create a new Matrix.
     */
    public Matrix() {
    }

    /**
     * Create a new Matrix.
     *
     * @param rows the rows
     * @param cols the cols
     */
    public Matrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.size = rows * cols;
        // init array to all zero
        a = new double[size];
    }

    /**
     * Create a new Matrix.
     *
     * @param rows the rows
     * @param cols the cols
     */
    public Matrix(int rows, int cols, double[] a) {
        this.rows = rows;
        this.cols = cols;
        this.size = rows * cols;
        this.a = Arrays.copyOf(a, size);
    }

    /**
     * Create a new Matrix.
     *
     * @param rows the rows
     * @param cols the cols
     */
    public Matrix(int rows, int cols, double d) {
        this.rows = rows;
        this.cols = cols;
        this.size = rows * cols;
        this.a = new double[size];
        Arrays.fill(a, d);
    }

    public void checkNaN(String name){
        for(int k = 0; k < size; k++) {
            if (Double.isNaN(a[k])) {
                LOG.info("checkNaN name: " + name + ",size : " + size + ", a[k] : " + a[k]  + ", k: " + k);
                int n = Math.min(10, size);
                MTX.logMatrx(name,this,  n);
                throw new RuntimeException("a[k] is NaN");
            }
        }
    }
    @Override
    public String toString() {
        int len = 0;
        if(a != null){
            len = a.length;
        }
        return "{" +
                "rows=" + rows +
                ", cols=" + cols +
                ", size=" + size +
                ", array len=" + len +
                '}';
    }
} // end class