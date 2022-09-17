package datasci.backend.activations;

import datasci.backend.model.MTX;
import datasci.backend.model.Matrix;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LeakyReluActivation implements ActivationI {
    private static final Logger LOG = Logger.getLogger(LeakyReluActivation.class.getName());
    private Matrix dYdZ;
    private String actName = ActE.LEAKY_RELU.label;
    private double leftSlope = 0.1;
    private double rightSlope = 1.0;
    //
    public LeakyReluActivation() {

    }
    public String getActName() {
        return actName;
    }

    /**
     * Activation function for training phase
     * Find transformation y = S(z), where S is the activation function, z is input matrix
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
        // if s(z) = sigmoid function (activation function)
        // ds/dz = s(z)[1 - s(z)]
        for (int k = 0; k < z.size; k++) {
            double zc = z.a[k];
            //
            if(zc > 0){
                // right slope = 1.0
                y.a[k] = rightSlope * zc;
                dYdZ.a[k] = rightSlope;
            } else if(zc <= 0) {
                y.a[k] = leftSlope * zc;
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
        // let s(z) = sigmoid function (activation function)
        for (int k = 0; k < z.size; k++) {
            if(z.a[k] > 0){
                // right slope = 1.0
                y.a[k] = z.a[k];
            } else {
                y.a[k] = leftSlope*z.a[k];
            }
        }
        return y;
    }

    /**
     * Find derivative matrix dF/dZ of activation function S with respect to input matrix z
     *
     * @return dF/dZ derivative matrix of activation function S with respect to input z
     */
    public Matrix derivative() {
        return dYdZ;
    }
}
