package datasci.backend.layers;

import datasci.backend.activations.ActivationI;
import datasci.backend.model.MTX;
import datasci.backend.model.MathUtil;
import datasci.backend.model.Matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
  Network Internal Layer.
  //
  backprop ref:
  https://mattmazur.com/2015/03/17/a-step-by-step-backpropagation-example/

 Batch: a small number of samples processed through the forward propagation and back
 propagation before performing updates on the weights and biases. The purpoase of the
 batch is to smooth out the updates. If the batch size is 20 samples, and if there
 are 20,000 samples, the number of batches would be 1000. A batch size of 1 means
 updates are performed on every sample.
 //
 dL/dW = change in loss due to change in weight
 batch ave[dL/dW] = average of dL/dW over batch size
 dL/dB = change in loss due to change in bias
 batch ave[dL/dB] = average of dL/dB over batch size
 //
 Eta: gradient descent rate, is defined in a function to vary by sample count, and is a
 multiplier factor for dL/dW and dL/dB.
 //
 Momentum v: exponential average of the gradient steps
 mu: momentum factor, close to 1, usually about 0.9
 Momentum update after each batch from i to i+1:
 v(i + 1) = mu * v(i) - eta * batch ave[dL/dW]
 //
 L2 regularization factor, lambda: small number which reduces the weight matrix on each update.
 //
 Weight correction after each batch from i to i+1:
 W(i + 1) = (1 - lambda)*W(i) + v(i + 1)
 See MathUtil. updateWeightMatrix for implementation details.
 //
 Bias correction after each batch from i to i+1:
 B(i + 1) = B(i) – eta * batch ave[dL/dB]
 //
 How to calculate dL/dW?
 dL/dW = (dL/dY)*(dY/dZ)*(dZ/dW)
 Recall Z = W*X + B
 Recall Y = S(Z) where S(Z) is the activation function
 dL/dY is known from previous layer
 dY/dZ depends on which activation function is applied
 dZ/dW = X
 //
 How to calculate dL/dB?
 dL/dB = (dL/dY)*(dY/dZ)*(dZ/dB)
 dL/dY is known from previous layer
 dY/dZ depends on which activation function is applied
 dZ/dB = I
//
 Find dL/dX for back propagation:
 dL/dX = (dL/dY)*(dY/dZ)*(dZ/dX)
 dL/dY is known from previous layer
 dY/dZ depends on which activation function is applied
 dZ/dX = W
//
 How to back propagate to previous layer?
 dL/dY previous layer = dL/dX current layer
 Apply dL/dW and dL/dB calculations as above to update weight and bias in previous layer.
 Proceed to calculate dL/dX in previous layer for back prop input (dL/dY) to next previous layer.


 */
public class InternalLayer {

    private static final Logger LOG = Logger.getLogger(InternalLayer.class.getName());

    //
    private final LayerE layerType = LayerE.INTERNAL;
    // number of nodes in this layer
    private int nOut;
    // number of inputs to each node (number of nodes in previous layer)
    private int nIn;
    // activation function and derivative
    private ActivationI actFn;
    // one column matrix is the input from previous layer
    private List<Matrix> inList;
    // column vector x is the input from previous layer with nIn rows
    private Matrix x;
    // weight matrix: nOut rows, nIn columns
    private Matrix w;
    // velocity matrix: nOut rows, nIn columns
    private Matrix v;
    // bias matrix: 1 col, nOut rows
    private Matrix b;
    // output matrix with one column and nOut rows, y = actFn(z) becomes input x for next layer
    private Matrix y;
    //
    // back propagation
    //
    // ref: https://towardsdatascience.com/deriving-the-backpropagation-equations-from-scratch-part-2-693d4162e779
    //
    // eta: gradient descent rate for back propagation
    private double eta;
    // lambda: L2 regularization parameter
    private double lambda;
    private double oneMinusLambda;
    // mu:  momentum parameter
    private double mu;
    //
    private int batchCount;
    private List<Matrix> batchWeight = new ArrayList<>();
    private List<Matrix> batchBias = new ArrayList<>();

    // to reproduce results, use same seed for internal layer weights
    private static long INTERNAL_WT_SEED = 1234;
    //
    // ID for debug purposes
    private String layerID;
    // doNow flag for debug logging
    private boolean doNow;

    /**
     * Instantiates a new Internal layer.
     */
    public InternalLayer() {
    }

    /**
     * Instantiates a new Internal layer.
     *
     * @param nOut  number of output nodes
     * @param actFn the activation function
     */
    public InternalLayer(int nIn, int nOut, ActivationI actFn) {
        this.nIn = nIn;
        this.nOut = nOut;
        this.actFn = actFn;
    }

    public String getLayerID() {
        return layerID;
    }

    public void setLayerID(String layerID) {
        this.layerID = layerID;
    }
    public void setDoNow(boolean doNow) {
        this.doNow = doNow;
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

    public void initLayer() {
        initWeight();
        initBias();
        initVelocity();
    }

    /**
     * Init weight matrix
     */
    public void initWeight() {

        try {
            // weight matrix w will be matrix with outputs rows and inputs columns
            // let nOut = number output nodes, nIn = number of input nodes
            // W(nOut, nIn)
            w = new Matrix(nOut, nIn);
            LOG.info("w rows: " + nIn + ", cols: " + nOut);
            // standard deviation for initial weight
            // kaiming
            double stdDev = Math.sqrt(2.0 / nIn);
            LOG.info("stdDev: " + stdDev);
            // xavier
            // double stdDev = Math.sqrt(6.0 / (nOut + nIn));
            // to reproduce results, use same seed
            Random r = new Random(INTERNAL_WT_SEED);
            for (int k = 0; k < w.size; k++) {
                // initialize each weight to random gaussian with mean zero
                w.a[k] = r.nextGaussian() * stdDev;
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
     * Init b.
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
     * @return matrix output y from this layer; treat matrix y as a one column matrix
     */
    public Matrix trainForward(Matrix x) {
        this.x = x;
        try {
            // z = W*X + b, where column vector X is the input from previous layer
            // z has nOut rows and nIn columns
            Matrix z = MTX.aXplusB(w, x, b);
            y = actFn.trainingFn(z);
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
        List<Matrix> outList = new ArrayList<>();
        nIn = x.rows;

        // z = W*X + b, where column vector X is the input from previous layer
        // z has nOut rows and nIn columns
        Matrix z = MTX.aXplusB(w, x, b);
        //
        y = actFn.testingFn(z);
        //
        return y;
    }

    /**
     * Perform backward propagation for this network layer
     *
     * @param dLdY derivative of cost function with respect to layer output y
     * @return derivative of layer output cost with respect to layer input dL/dX
     */
    public Matrix backProp(Matrix dLdY, boolean batchCompleted) {
        Matrix dLdX = null;
        try {
            // dLdY(n,1) column vector
            //
            // output dLdX: d(cost)/d(input x)
            // let n = number output nodes, m = number of input nodes
            // Z(n,1) = W(n,m) * x(m,1) + bias(n,1)
            // Z = W * x + b
            // dZdW = x transpose
            // dZdB = 1
            // dYdZ is derivative of activation function
            // dLdZ = dLdY * dYdZ
            // dLdW = dLdZ * dZdW = dLdZ * x
            // dLdB = dLdZ * dZdB = dLdZ * 1
            //
            // dZdX = w
            // dLdX(1, m) = dLdZ(1,n) * dZdX(n,m)
            // dLdX = dLdZ * w
            //
            // let n = number output nodes, m = number of input nodes
            // dYdZ(n,1): derivative of activation function
            Matrix dYdZ = actFn.derivative();
            // dLdZ(n,1) = dLdY(n,1) ** dYdZ(n,1), cell multiply
            Matrix dLdZ = MTX.cellMult(dLdY, dYdZ);
            //  dZdW(1,m) <- x(m, 1)
            Matrix dZdW = MTX.colToRow(x);
            // dZdB = 1;
            // dLdB(n,1) = dLdZ * dZdB =  dLdZ * 1
            Matrix dLdB = dLdZ;
            // dZdX(n,m), w(n, m)
            Matrix dZdX = w;
            LOG.fine("dZdX : " + dZdX);
            // derivative of layer output cost with respect to layer input
            // dLdX(1, m) = dLdZ(1,n) * dZdX(n,m)
            Matrix dLdZrow = MTX.colToRow(dLdZ);
            Matrix dLdXrow = MTX.mult(dLdZrow, dZdX);
            dLdX = MTX.rowToCol(dLdXrow);
            //
            // on backprop, pass single col matrix
            // dLdXCol will become the backProp dLdY for the previous layer
            //
            // complete other calculations before updating w and b
            //
            // dLdW(n, m) = dLdY(n, 1) ** dYdZ(n,1) * dZdW(1,m)
            // dLdY(n, 1) ** dYdZ(n,1) was done above
            // dLdW(n, m) = dLdZ(n,1) * dZdW(1,m)
            Matrix dLdW = MTX.mult(dLdZ, dZdW);
            LOG.fine("dLdW : " + dLdW);
            // db(n, 1) = dLdB(n,1)*eta
            Matrix dB = MTX.mulConstant(dLdB, -eta);
            //
            batchWeight.add(dLdW);
            batchBias.add(dB);
            if(batchCompleted) {
                w.checkNaN("InternalLayer ID: " + layerID + ", batch w");
                dLdW = MTX.listAverage(batchWeight);
                MathUtil.updateWeightMatrix(dLdW, eta, w, v, mu, oneMinusLambda);
                //
                // update bias matrix
                LOG.fine("bias b: " + b);
                // bias(n, 1) = bias(n, 1) + db(n, 1)
                dB = MTX.listAverage(batchBias);
                MTX.addInplace(b, dB);
                //
                batchCount = 0;
                batchWeight.clear();
                batchBias.clear();
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }

        // dLdX(m, 1), pass to previous layer as dLdY
        return dLdX;
    }

} // end class