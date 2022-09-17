package datasci.backend.model;

import datasci.backend.activations.LeakyReluActivation;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.OptionalDouble;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;

/**
 * Matrix in one-D row format
 */
public class Matrix {
    private static final Logger LOG = Logger.getLogger(LeakyReluActivation.class.getName());
    private static final DecimalFormat fmtThree = new DecimalFormat("0.###E00");
    //
    public int rows;
    public int cols;
    public int size;
    public double[] a;

    /**
     * Create a new Matrix. Zero rows, cols, size.
     */
    public Matrix() {
    }

    /**
     * Copy constructor
     */
    public Matrix(Matrix old) {
        this.rows = old.rows;
        this.cols = old.cols;
        this.size = rows * cols;
        // init array to old matrix
        this.a = Arrays.copyOf(old.a, size);

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
            if (Double.isNaN(a[k]) || Double.isInfinite(a[k])) {
                LOG.info("checkNaN name: " + name + ",size : " + size + ", a[k] : " + a[k]  + ", k: " + k);
                int n = Math.min(5, size);
                MTX.logMatrx(name,this,  n);
                throw new RuntimeException("a[k] is NaN");
            }
        }
    }
    @Override
    public String toString() {

        int len = 0;
        String msg = "";
        String maxCell = "";
        String minCell = "";
        String sumCells = "";
        String aveCells = "";
        try{
        if(a != null){
            // len must match size, and size must = rows * cols
            len = a.length;

            if(len != size){
                msg = " len does not match size ";
            }
            DoubleStream maxStream = DoubleStream.of(a);
            OptionalDouble maxOpt = maxStream.max();
            if (maxOpt.isPresent()) {
                maxCell = fmtThree.format(maxOpt.getAsDouble());
            }
            DoubleStream minStream = DoubleStream.of(a);
            OptionalDouble minOpt = minStream.min();
            if (minOpt.isPresent()) {
                minCell = fmtThree.format(minOpt.getAsDouble());
            }
            DoubleStream sumStream = DoubleStream.of(a);
            double sumOpt = sumStream.sum();
                sumCells = fmtThree.format(sumOpt);
            DoubleStream aveStream = DoubleStream.of(a);
            OptionalDouble  aveOpt = aveStream.average();
            if (aveOpt.isPresent()) {
                aveCells = fmtThree.format(aveOpt.getAsDouble());
            }
        }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return "{" +
                "rows=" + rows +
                ", cols=" + cols +
                ", size=" + size +
                ", array len=" + len +
                 "" + msg +
                ", first=" + a[0] +
                ", last=" + a[len - 1] +
                ", minVal=" + minCell +
                ", maxVal=" + maxCell +
                ", sumCells=" + sumCells +
                ", aveCells=" + aveCells +
                '}';
    }
} // end class