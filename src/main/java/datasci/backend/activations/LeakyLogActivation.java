package datasci.backend.activations;

import datasci.backend.model.MTX;
import datasci.backend.model.Matrix;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LeakyLogActivation implements ActivationI {

    private static final Logger LOG = Logger.getLogger(LeakyLogActivation.class.getName());
    private Matrix dYdZ;
    private String actName = ActE.LEAKY_LOG.label;
    private double leftSlope = 0.1;

    //
    public LeakyLogActivation() {

    }

    public String getActName() {
        return actName;
    }
    /**
     * Activation function for training phase
     * Find transformation y = F(z), where F is the activation function, z is input matrix
     * For training activation, there will be back propagation
     * Find derivative of activation function at same time, and save for back propagation
     *
     * @param z input column matrix to activation function F
     * @return transformation output column matrix y
     */
    public Matrix trainingFn(Matrix z) {
        // activation function output y = F(z)
        Matrix y = new Matrix(z.rows, z.cols);
        try{
            // save derivative of activation function for back propagation
            dYdZ = new Matrix(z.rows, z.cols);
            for (int k = 0; k < z.size; k++) {
                double zk = z.a[k];
                //
                if(zk > 1.0){
                    // right slope = 1.0
                    y.a[k] = Math.log(zk);
                    dYdZ.a[k] = 1.0 / zk;
                } else if(zk <= 1.0) {
                    y.a[k] = leftSlope * (zk - 1.0);
                    dYdZ.a[k] = leftSlope;
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return y;
    }
    /**
     * Activation function for testing phase
     * Find transformation y = F(z), where F is the activation function, z is input matrix
     * For test activation, there is no back propagation
     *
     * @param z input column matrix to activation function F
     * @return transformation output column matrix y
     */
    public Matrix testingFn(Matrix z) {
        // activation function output y = F(z)
        Matrix y = new Matrix(z.rows, z.cols);
        try{
            for (int k = 0; k < z.size; k++) {
                double zk = z.a[k];
                //
                if(zk > 1.0){
                    // right slope = 1.0
                    y.a[k] = Math.log(zk);
                } else if(zk <= 1.0) {
                    y.a[k] = leftSlope * (zk - 1.0);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return y;
    }

    /**
     * Find derivative matrix dF/dZ of activation function F with respect to input matrix z
     *
     * @return dF/dZ derivative matrix of activation function F with respect to input z
     */
    public Matrix derivative() {
        return dYdZ;
    }
}
