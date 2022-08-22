package datasci.backend.activations;

import datasci.backend.model.Matrix;

public class SigmoidActivation implements ActivationI {

    private Matrix dYdZ;
    public SigmoidActivation() {

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
        // save derivative of activation function for back propagation
        dYdZ = new Matrix(z.rows, z.cols);
        // let s(z) = sigmoid function (activation function)
        // ds/dz = s(z)[1 - s(z)]
        for (int k = 0; k < z.size; k++) {
            double s = 1 / (1 + Math.exp(-z.a[k]));
            y.a[k] = s;
            dYdZ.a[k] = s * (1 - s);
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
            double s = 1 / (1 + Math.exp(-z.a[k]));
            y.a[k] = s;
        }
        return y;
    }

    /**
     * Find derivative matrix dF/dZ of activation function F with respect to input matrix z
     *
     * @return dF/dZ derivative matrix of activation function F with respect to input z
     */
    public Matrix derivative() {
        // let s(z) = sigmoid function
        // ds/dz = s(z)[1 - s(z)]
        return dYdZ;
    }
}
