package datasci.backend.activations;

import datasci.backend.model.MTX;
import datasci.backend.model.Matrix;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SoftmaxActivation implements ActivationI {
    private static final Logger LOG = Logger.getLogger(SoftmaxActivation.class.getName());

    private Matrix dYdZ;
    private String actName = ActE.SOFTMAX.label;

    // ref: https://www.mldawn.com/wp-content/uploads/2020/05/backprop-softmax-cross-8-1024x575.png
    public SoftmaxActivation() {
    }

    public String getActName() {
        return actName;
    }

    /**
     * Activation function for training phase
     * Find transformation y = F(z), where F is the activation function, z is input matrix
     * For training activation, there will be back propagation
     *
     * @param z input column matrix to activation function F
     * @return softmax transformation, output column matrix y
     */
    public Matrix trainingFn(Matrix z) {
        // softmax activation function output y = F(z)
        Matrix y = new Matrix(z.rows, z.cols);
        try {
            // To make the softmax function numerically stable, normalize the values,
            // by multiplying the numerator and denominator with a constant M.
            // let log(M) = −max(z),  M = exp(- maxZ)
            //
            double maxZ = MTX.maxCell(z);
            double total = Arrays.stream(z.a).map(u -> Math.exp(u - maxZ)).sum();
            LOG.fine("maxZ : " + maxZ + ", total: " + total);
            LOG.fine("z size: " + z.size);
            LOG.log(Level.FINE,"y : " + y);
            for (int k = 0; k < z.size; k++) {
                // softmax output y = F(z) based on z input
                y.a[k] = Math.exp(z.a[k] - maxZ) / total;
                //
                // Note: all y values are always positive (0 to 1) due to softmax function
                //
                /*
                if (Double.isNaN(y.a[k])) {
                    double zz = z.a[k] - maxZ;
                    LOG.info("maxZ : " + maxZ + ", total: " + total);
                    LOG.info("z.a[k] : " + z.a[k] + ", zz: " + zz + ", k: " + k);
                 //   throw new RuntimeException("y is NaN");
                }

                 */
                //
                // compute derivative dYdZ
                createDeriv(y);
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
        // softmax activation function output y = F(z)
        Matrix y = new Matrix(z.rows, z.cols);
        try {
            // activation function output y = F(z)
            // To make the softmax function numerically stable, normalize the values,
            // by multiplying the numerator and denominator with a constant M.
            // let log(M)=−max(z)
            double maxZ = MTX.maxCell(z);
            double total = Arrays.stream(z.a).map(u -> Math.exp(u - maxZ)).sum();
            LOG.fine("maxZ : " + maxZ + ", total: " + total);
            for (int k = 0; k < z.size; k++) {
                // softmax output F(z) based on z input
                y.a[k] = Math.exp(z.a[k] - maxZ) / total;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return y;
    }

    /**
     * Create softmax derivative dYdZ, derivative of y with respect to z, where y = F(z)
     * Save derivative of activation function for back propagation
     *
     * dYdZ: square matrix with #rows = #cols = output rows
     *
     *    dYdZ = new Matrix(z.rows, z.rows);
     *
     *    dY(zi)dZi = Y(zi)*(1 - Y(zi))
     *
     *    dY(zi)dZj = -Y(zi)Y(zj)
     *
     * @param y predicted softmax vector
     */
    public void createDeriv(Matrix y) {
        //derivative dYdZ, where y = Y(z)
        // z is activation input vector
        // y is predicted output from activation function
        dYdZ = new Matrix(y.rows, y.rows);
        double val = 0;
        for (int i = 0; i < y.size; i++) {
            double yi = y.a[i];
            for (int j = 0; j < y.size; j++) {
                double yj = y.a[j];
                if(i == j) {
                    val = yi * (1 - yi);
                } else{
                    val = -yi * yj;
                }
                MTX.setCell(dYdZ, i, j, val);
            }
        }
    }

    /**
     * Find derivative matrix dY/dZ of activation function Y with respect to input matrix z
     *
     * @return dY/dZ derivative matrix of activation function F with respect to input z
     */
    public Matrix derivative() {
        return dYdZ;
    }
}
