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
 * Network Internal Layer.
 * <p>
 * backprop ref:
 * https://mattmazur.com/2015/03/17/a-step-by-step-backpropagation-example/
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

    // to reproduce results, use same seed for internal layer weights
    private static long INTERNAL_WT_SEED = 1234;
    //

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
            LOG.fine("w : " + w);
            LOG.fine("x: " + x);

            double maxX = MTX.maxAbsCell(x);
            if(maxX > MTX.Hi_LIMIT){
                LOG.info("maxX: " + maxX);
            }

            //
            // z = W*X + b, where column vector X is the input from previous layer
            // z has nOut rows and nIn columns
            Matrix z = MTX.aXplusB(w, x, b);
            z.checkNaN("internal train z after aXplusB");
            double maxZ = MTX.maxAbsCell(z);
            if(maxZ > MTX.Hi_LIMIT){
                LOG.info("maxZ: " + maxZ);
            }
            MTX.normalizeInPlace(z);
            double minZ = MTX.minAbsCell(z);
            if(minZ < MTX.Low_LIMIT){
                LOG.info("minZ: " + minZ);
            }

            //
            z.checkNaN("internal z before actFn");
            y = actFn.trainingFn(z);
            LOG.fine("y: " + y);
            //
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        LOG.fine("y: " + y);
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
    public Matrix backProp(Matrix dLdY) {
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
            //  dZdW(1,m) <- x(m, 1)
            Matrix dZdW = MTX.colToRow(x);
            // dLdB(n,1) = dLdZ * 1
            Matrix dLdB = dYdZ;
            // dLdZ(n,1) = dLdY(n,1) ** dYdZ(n,1), cell multiply
            Matrix dLdZ = MTX.cellMult(dLdY, dYdZ);
            LOG.fine("dLdY : " + dLdY);
            LOG.fine("dYdZ : " + dYdZ);
            LOG.fine("dLdZ : " + dLdZ);
            //
            LOG.fine("dZdW : " + dZdW);
            // dZdX(n,m), w(n, m)
            Matrix dZdX = w;
            LOG.fine("dZdX : " + dZdX);
            // derivative of layer output cost with respect to layer input
            // dLdX(1, m) = dLdZ(1,n) * dZdX(n,m)
            Matrix dLdZrow = MTX.colToRow(dLdZ);
            Matrix dLdXrow = MTX.mult(dLdZrow, dZdX);
            dLdX = MTX.rowToCol(dLdXrow);
            LOG.fine("dLdX : " + dLdX);
            // on backprop, pass single col matrix
            // dLdXCol will become the backProp dLdY for the previous layer
            //
            // complete other calculations before updating w and b
            //
            //
            // dLdW(n, m) = dLdY(n, 1) ** dYdZ(n,1) * dZdW(1,m)
            // dLdY(n, 1) ** dYdZ(n,1) was done above
            // dLdW(n, m) = dLdZ(n,1) * dZdW(1,m)
            Matrix dLdW = MTX.mult(dLdZ, dZdW);
            LOG.fine("dLdW : " + dLdW);
            MathUtil.updateWeightMatrix(dLdW, eta, w, v, mu, oneMinusLambda);
            /*
            // update weight matrix
            // dw(n, m) = dLdW(n, m) * eta
            Matrix dw = MTX.mulConstant(dLdW, -eta);
            // update velocity matrix, initially zero
            MTX.mulConstInPlace(v, mu);
            // update velocity matrix with gradient
            // v = mu * v - eta * dLdW
            MTX.addInplace(v, dw);

            // L2 regularization to reduce weights on back prop
            // W(n, m) = W(n, m) * (1 - lambda)
            MTX.mulConstInPlace(w, oneMinusLambda);
            // w(n, m) = w(n, m) + dw(m, n)
            MTX.addInplace(w, dw);

             */

            //
            // update b matrix
            // db(n, 1) = dLdB(n,1)*eta
            Matrix db = MTX.mulConstant(dLdB, -eta);
            LOG.fine("db : " + db);
            MTX.addInplace(b, db);
            //
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }

        // dLdX(m, 1), pass to previous layer as dLdY
        return dLdX;
    }

} // end class