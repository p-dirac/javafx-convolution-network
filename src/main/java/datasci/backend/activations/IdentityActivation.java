package datasci.backend.activations;

import datasci.backend.model.MTX;
import datasci.backend.model.Matrix;

import java.util.logging.Logger;

public class IdentityActivation implements ActivationI {
    private static final Logger LOG = Logger.getLogger(IdentityActivation.class.getName());
    private Matrix dYdZ;
    private String actName = ActE.IDENT.label;
    public IdentityActivation() {
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
     * @param z input matrix to activation function F
     * @return transformation output matrix y
     */
    public Matrix trainingFn(Matrix z) {
        // activation function output y = F(z)
        Matrix y = z;
        // save derivative of activation function for back propagation
        // slope = 1.0
        dYdZ = MTX.createIdentMatrix(z.rows, z.cols);
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
        Matrix y = z;
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
