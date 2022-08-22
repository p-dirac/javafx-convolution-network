package datasci.backend.layers;

import datasci.backend.activations.ActivationI;
import datasci.backend.model.MTX;
import datasci.backend.model.MathUtil;
import datasci.backend.model.Matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  Network Output Layer
 */
public class OutputLayer {

    private static final Logger LOG = Logger.getLogger(OutputLayer.class.getName());

    //
    private final LayerE layerType = LayerE.OUTPUT;
    // number of nodes in this layer
    private int nOut;
    // number of inputs to each node (number of nodes in previous layer)
    private int nIn;
    // activation function and derivative
    private ActivationI actFn;
    // one column matrix is the input from previous layer
    private List<Matrix> inList;
    // input column vector x is the output Y from previous layer
    private Matrix x;
    // weight matrix: nOut rows, nIn columns
    private Matrix w;
    // velocity matrix: nOut rows, nIn columns
    private Matrix v;
    // bias matrix: 1 col, nOut rows
    private Matrix b;
    // predicted output matrix with one column and nOut rows, yOut = actFn(z) becomes input x for next layer
    private Matrix y;
    //
    // back propagation
    //
    // eta: gradient descent rate for back propagation
    private double eta;
    // lambda: L2 regularization parameter
    private double lambda;
    private double oneMinusLambda;
    // mu:  momentum parameter
    private double mu;

    // to reproduce results, use same seed for output layer weights
    private static long OUTPUT_WT_SEED = 4321;
    // When prediction is correct, prediction = max, and MAX_TOL allows for rounding discrepancy
    private static double MAX_TOL = 1.0E-6;
    //


    // actual output column matrix with one column and nOut rows
    private Matrix actualY;
    private List<Matrix> batchLoss = new ArrayList<>();
    //
    // For backprop see ref:
    //   https://web.eecs.umich.edu/~justincj/teaching/eecs442/notes/linear-backprop.html
    //   https://www.mldawn.com/wp-content/uploads/2020/05/backprop-softmax-cross-8-1024x575.png
    //   https://www.analyticsvidhya.com/blog/2021/06/how-does-backward-propagation-work-in-neural-networks/
    //

    /**
     * Instantiates a new Output layer.
     */
    public OutputLayer() {
    }

    /**
     * Instantiates a new Output layer.
     *
     * @param nIn   number of input nodes
     * @param nOut  number of output nodes
     * @param actFn the activation function
     */
    public OutputLayer(int nIn, int nOut, ActivationI actFn) {
        this.nIn = nIn;
        this.nOut = nOut;
        this.actFn = actFn;
        LOG.info("OutputLayer, nIn: " + nIn + ", nOut: " + nOut);
    }

    public LayerE getLayerType() {
        return layerType;
    }

    public void setEta(double eta) {
        this.eta = eta;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
        this.oneMinusLambda = 1.0 - lambda;
    }

    public void setMu(double mu) {
        this.mu = mu;
    }

    public void setActualY(Matrix actualY) {
        this.actualY = actualY;
    }

    public void initLayer() {
        initWeight();
        initBias();
        initVelocity();
    }

    /**
     * Init weight.
     */
    public void initWeight() {
        try {
            // weight matrix w will be matrix with outputs rows and inputs columns
            // let nOut = number output nodes, nIn = number of input nodes
            // W(nOut, nIn)
            w = new Matrix(nOut, nIn);
            LOG.info("w rows: " + nIn + ", cols: " + nOut);
            // desired standard deviation for initial weight
            double stdDev = Math.sqrt(2.0 / nIn);
            LOG.info("stdDev: " + stdDev);
            Random r = new Random(OUTPUT_WT_SEED);
            for (int k = 0; k < w.size; k++) {
                // initialize each weight to random gaussian with mean zero
                w.a[k] = r.nextGaussian() * stdDev;
                //
                // just uniform with mean zero
            //    w.a[k] = (r.nextDouble() - 0.5);
                // just uniform 0 to 1
             //   w.a[k] = r.nextDouble();

            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }

    }
    /**
     * Init velocity matrix.
     */
    public void initVelocity() {
        try {
            // velocity matrix w will be matrix with outputs rows and inputs columns
            v = new Matrix(nOut, nIn);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Init bias.
     */
    public void initBias() {
        // bias matrix w will be matrix with nOut rows and 1 columns
        // default values = zero
        b = new Matrix(nOut, 1);
    }

    public Matrix getW() {
        return w;
    }

    public Matrix getB() {
        return b;
    }

    public void setW(Matrix w) {
        this.w = w;
    }

    public void setB(Matrix b) {
        this.b = b;
    }

    /**
     * Perform forward propagation for this network layer
     *
     * @param x input to this layer; treat matrix x as a one column matrix
     * @return matrix output y from this layer; treat matrix yOut as a one column matrix
     */
    public Matrix trainForward(Matrix x) {
        try {
            LOG.fine("x: " + x);
            this.x = x;
            nIn = x.rows;

            LOG.fine("w : " + w);
            LOG.fine("x : " + x);
         //   x.checkNaN("output trainForward x");
            // z = W*X + b, where input X column vector is the output from previous layer
            // z has nOut rows and nIn columns
            Matrix z = MTX.aXplusB(w, x, b);
            z.checkNaN("output z");
         //   MTX.normalize(z);
            LOG.log(Level.FINE,"z : " + z);
            //
            // predicted Y output
            y = actFn.trainingFn(z);
            LOG.log(Level.FINE,"y : " + MathUtil.arraytoString(y.a));
            //
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }

        return y;
    }

    /**
     * Testing phase has no back propagation, use testing activation function
     * Perform forward propagation for this network layer
     *
     * @param x input to this layer
     * @return matrix output y from this layer
     */
    public Matrix testForward(Matrix x) {
        this.x = x;
        nIn = x.rows;
        // z = W*X + b, where column vector X is the input from previous layer
        // z has nOut rows and nIn columns
        Matrix z = MTX.aXplusB(w, x, b);
        //
        // predicted Y output
        y = actFn.testingFn(z);
        //
        return y;
    }
    /**
     * Update derivative loss matrix and save in list
     * Loss function: negative log likelihood
     * Loss = - sum { (actual y) * ln(predicted y) },
     * where the sum is over the classes, not the samples.
     * We don't need the loss, just the derivative of the loss.
     *
     * Derivative of loss function, L, with respect to z, dL/dZ,
     * where y = S(z), and S is softmax activation function;
     * use chain rule:
     * dL/dZ = dL/dY * dY/dZ
     * dL/dZ = predicted y - actual y
     *
     * Note: predicted y and actual y must be set before this call
     *
     */
    public void updateBatchLoss() {
        try {
            //
            LOG.fine("output layer");
            LOG.fine("actualMatrix: " + MathUtil.arraytoString(actualY.a));
            // predicted y = softmax[k]
            // column matrix loss, dLdZ[k] = predicted y[k] -  actual y[k]
            Matrix dLdZ = MTX.subtract(y, actualY);
            //
            // Note: all y values are always positive (0 to 1) due to softmax function
            //

            LOG.fine("dLdZ: " + MathUtil.arraytoString(dLdZ.a));
            batchLoss.add(dLdZ);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }


        /**
         * Perform backward propagation for this network layer
         *
         * @param dLdZ derivative of loss function with respect to activation input Z
         * @return derivative of layer output loss with respect to layer input dL/dX
         */
        public Matrix backProp(Matrix dLdZ) {
            Matrix dLdX = null;
            try {
                // let n = number output nodes, m = number of input nodes
                // dLdZ(n,1) column vector
                // Z(n,1) = mW(n,m) * x(m,1) + bias(n,1)
                // Z = mW * x + bias
                // dZdW = x transpose
                // dZdB = 1
                // dLdW = dLdZ*dZdW = dLdZ*x
                // dLdB = dLdZ*dZdB = dLdZ*1
                // dLdZ(n,1)
                // Matrix dLdZ = lossFn();
                //
                // dZdX = w
                // dLdX(1, m) = dLdZ(1,n) * dZdX(n,m)
                //
                //  dZdW(1,m) <- x(m, 1)
                Matrix dZdW = MTX.colToRow(x);
                // dZdX(n,m), mW(n, m)
                Matrix dZdX = w;
                LOG.fine("dLdZ: " + dLdZ);
                LOG.fine("dZdW : " + dZdW);
                LOG.fine("dZdX, : " + dZdX);
                //   LOG.info("dZdX.a, : " +  MathUtil.arraytoString(dZdX.a));

                // derivative of layer output cost with respect to layer input
                // dLdX(1, m) = (dLdZ(n,1) transpose) * dZdX(n,m)
                // here dLdX will be 1 row, n cols
                Matrix dLdZrow = MTX.colToRow(dLdZ);
                Matrix dLdXrow = MTX.mult(dLdZrow, dZdX);
                dLdX = MTX.rowToCol(dLdXrow);
                //
                // dLdX col vector will become the backProp dLdY col vector for the previous layer
                //
                LOG.fine("dLdX : " + dLdX);
                //   LOG.info("dLdZ.a: " + MathUtil.arraytoString(dLdZ.a));
                //   LOG.info("dLdX.a: " + MathUtil.arraytoString(dLdX.a));

                //
                // complete dLdX calculations before updating mW and bias
                //
                // dLdW(n, m) = dLdZ(n,1) * dZdW(1,m)
                Matrix dLdW = MTX.mult(dLdZ, dZdW);
                LOG.fine("dLdW : " + dLdW);

                MathUtil.updateWeightMatrix(dLdW, eta, w, v, mu, oneMinusLambda);
                /*
                // update delta weight matrix
                // dw(n, m) = - eta * dLdW(n, m)
                Matrix dw = MTX.mulConstant(dLdW, -eta);
                // update velocity matrix, initially zero
                MTX.mulConstInPlace(v, mu);
                // update velocity matrix with gradient
                // v = mu * v - eta * dLdW
                MTX.addInplace(v, dw);
                // L2 regularization to reduce weights on back prop
                // W(n, m) = W(n, m) * (1 - lambda)
                MTX.mulConstInPlace(w, oneMinusLambda);
                // update weight matrix
                // W(n, m) = W(n, m) + dw(m, n)
                MTX.addInplace(w, dw);
                w.checkNaN("output backprop w");
                LOG.fine("after update, w : " + w);

                 */

                //
                // update bias matrix
                // db(n, 1) = dLdZ(n,1)*eta
                Matrix db = MTX.mulConstant(dLdZ, -eta);
                LOG.fine("db : " + db);
                LOG.fine("bias b: " + b);
                // bias(n, 1) = bias(n, 1) + db(n, 1)
                MTX.addInplace(b, db);
                b.checkNaN("output backprop b");
                //   MTX.logMatrx(b, 10);
                //
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
            return dLdX;
        }

    /**
     * Find derivative of loss function, dL/dZ, based on softmax activation,
     * where z is the activation input (z = w * x + b),
     * and y = S(z) ; note: y is 0 to 1 due to softmax function, S(z)
     * dL/dZ = dL/dY * dY/dZ
     * dL/dZ = predicted y - actual y
     *
     * @return derivative of layer output loss with respect to activation input z
     */
    public Matrix batchLossFn() {
        // dLdZ(n,1) with one column and nOut rows
        Matrix dLdZ = null;
        try {
            // output dLdZ: derivative of loss function with respect to activation input z
            // find average of batch loss matrices
            dLdZ = MTX.averageOfList(batchLoss);
        //
       //     dLdZ = MTX.listSum(batchLoss);
            // clear list for next batch
            batchLoss.clear();
            //   LOG.info("dLdZ average");
         //   MTX.logMatrx("batch loss, dLdZ", dLdZ, 10);
        //    LOG.info("ave dLdZ: " + MathUtil.arraytoString(dLdZ.a));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return dLdZ;
    }

    /**
     * Check if predicted class index is correct
     *
     * @param actualIndex true index (corresponding to current network input sample)
     * @return true if predicted class index equals actual index
     */
    public boolean isCorrect(int actualIndex) {
        boolean result = false;
        // output layer array y.a contains predictions for each class
        // maximum value used to check for predicted class index
        int predictedIndex = getPredictedIndex();
        //
        if (predictedIndex == actualIndex) {
            result = true;
        }
        LOG.fine(" result: " + result + ", actualIndex: " + actualIndex + ", predictedIndex: " + predictedIndex);
        return result;
    }
    /**
     * Check if predicted output value is correct
     *
     * @param actualIndex true index
     * @return true if predicted output index is correct
     */
    public boolean isCorrect_old(int actualIndex) {
        boolean result = false;
        // output layer array y.a contains predictions for each class
        // maximum value used to check for correct class
        double max = Arrays.stream(y.a).max().getAsDouble();
        //
        // Note: all y values are always positive (0 to 1) due to softmax function
        //
        //   LOG.info("predicted matrix y");
        //    MTX.logMatrx(y, 10);
        // use actualIndex as row index to predicted value
        // if predicted value is correct, it should equal max value
        double predicted = y.a[actualIndex];
        int predictedIndex = getPredictedIndex();
        // the correct predicted value should equal max
        // correct predicted value (0 to 1)
        // use MAX_TOL for rounding errors
        if ((Math.abs(predicted - max) < MAX_TOL)) {
            result = true;
        }
        LOG.fine("actualIndex: " + actualIndex + ", predictedIndex: " + predictedIndex);
        LOG.fine(" result: " + result + ", max: " + max + ", predicted: " + predicted);
        return result;
    }

    /**
     * In predicted y array find index of predicted value (i.e. maximum value)
     *
     * @return index of predicted value
     */
    public int getPredictedIndex() {
        // predicted index is index of max cell in predicted y column vector
        int predictedIndex = MathUtil.indexOfMax(y.a);
        return predictedIndex;
    }
    /**
     * In actual y array find index of actual value (i.e. maximum value)
     *
     * @return index of actual value
     */
    public int getActualIndex() {
        // actual index is index of max cell in actual y column vector
        int actualIndex = MathUtil.indexOfMax(actualY.a);
        return actualIndex;
    }


} // end class