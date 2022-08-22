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
 * Network Convolution Layer
 * reference:
 * https://www.analyticsvidhya.com/blog/2020/02/mathematics-behind-convolutional-neural-network/
 * for backprop pool layer: https://victorzhou.com/blog/intro-to-cnns-part-2/
 * <p>
 * Notes:
 * (Rows, columns) of input matrix = (n, n)
 * (Rows, columns) of filter matrix = (f,f)
 * (Rows, columns) of convolution output matrix = ((n-f+1) , (n-f+1))
 */
public class ConvoLayer {

    private static final Logger LOG = Logger.getLogger(ConvoLayer.class.getName());
    //
    private final LayerE layerType = LayerE.CONVO;
    // activation function and derivative
    private ActivationI actFn;
    // list of matrix is the input from previous layer
    private List<Matrix> inList;

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
    //
    private int filterSize;
    //private List<Matrix> filterList = new ArrayList<>();
    // filter matrix: numFilters rows, nIn columns
    private List<List<Matrix>> filterList = new ArrayList<>();
    // number of inputs (matrices) to each node (number of nodes in previous layer)
    private int nIn;
    // number of nodes (output filters) in this layer
    private int numFilters;
    // velocity matrix: same size as filterList
    private final List<List<Matrix>> velocityList = new ArrayList<>();
    // to reproduce results, use same seed for output layer weights
    private static final long OUTPUT_WT_SEED = 4321;


    private final Random filterRand = new Random(OUTPUT_WT_SEED);

    /**
     * Instantiates a new Internal layer.
     */
    public ConvoLayer() {
    }

    /**
     * Creates a new Convolution layer.
     *
     * @param actFn the activation function
     */
    public ConvoLayer(int nIn, int numFilters, ActivationI actFn, int filterSize) {
        this.nIn = nIn;
        this.numFilters = numFilters;
        this.actFn = actFn;
        this.filterSize = filterSize;
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

    public List<List<Matrix>> getFilterList() {
        return filterList;
    }

    public void setFilterList(List<List<Matrix>> filterList) {
        this.filterList = filterList;
        this.numFilters = filterList.size();
        initVelocityList(filterSize, numFilters);
    }

    public int getNumFilters() {
        return numFilters;
    }

    public void setNumFilters(int numFilters) {
        this.numFilters = numFilters;
    }

    public int getFilterSize() {
        return filterSize;
    }

    public void setFilterSize(int filterSize) {
        this.filterSize = filterSize;
    }

    /**
     * Init filter matrix
     * Note: call this method if filterList is not input
     */
    public Matrix initFilter(int filterSize) {
        this.filterSize = filterSize;
        //    LOG.info("initFilter, rows: " + filterSize + ", cols: " + filterCols);
        Matrix w = new Matrix(filterSize, filterSize);
        try {
            // standard deviation for initial weight
            double stdDev = Math.sqrt(2.0 / (filterSize * filterSize));
            LOG.fine("stdDev: " + stdDev);
            for (int k = 0; k < w.size; k++) {
                // initialize each weight to random gaussian with mean zero
                // scale the standard deviation
                w.a[k] = filterRand.nextGaussian() * stdDev;
            }
            LOG.fine("w: " + w);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return w;
    }

    public void initFilterList(int filterSize, int numFilters) {
        filterList.clear();
        this.filterSize = filterSize;
        LOG.info( "filterSize: " + filterSize + ", numFilters: " + numFilters + ", nIn: " + nIn);
        // numFilters: number of filter rows
        // filterList: one filter row = list of column filters
        for (int i = 0; i < numFilters; i++) {
            List<Matrix> colList = new ArrayList<>();
            // nIn: number of filter columns
            for (int j = 0; j < nIn; j++) {
                Matrix m = initFilter(filterSize);
                //Inserts element at specified position in this list.
                // Shifts the element currently at that position to the right (adds one to their indices).
                colList.add(j, m);
            }
        //    LOG.info( "colList size: " + colList.size());
            //Replaces the element at the specified position in this list with the specified element
            filterList.add(i, colList);
        }
        LOG.info( "filterList size: " + filterList.size());
        List<Matrix> filterRow = filterList.get(0);
        LOG.info( "filterRow size: " + filterRow.size());
        initVelocityList(filterSize, numFilters);
    }

    public void initVelocityList(int filterSize, int numFilters) {
        // velocityList: one velocity row = list of column velocities
        for (int i = 0; i < numFilters; i++) {
            List<Matrix> colList = new ArrayList<>();
            // nIn: number of velocity columns
            for (int j = 0; j < nIn; j++) {
                Matrix m = new Matrix(filterSize, filterSize);
                colList.add(m);
            }
            velocityList.add(colList);
        }
    }

    /**
     * Perform forward propagation for this network layer
     *
     * @param inList input to this layer; treat matrix x as rectangular, not a one column matrix
     * @return list of matrix output y from this layer; treat matrix y as a rectangular matrix
     */
    public List<Matrix> trainForward(List<Matrix> inList) {
        this.inList = inList;
        LOG.fine("trainForward");
        List<Matrix> outList = new ArrayList<>();
        try {
            // convolve matrix x with each filter f in filterList to create matrix z
            // matrix z: number of rows = x #rows - f #rows + 1
            // matrix z: number of cols = x #cols - f # cols + 1
            // output matrix y: z #rows, z #cols
            //
            // numIn: number of matrices to convolve
            int numIn = inList.size();
            // first x matrix for size
            Matrix x = inList.get(0);
            int zrows = x.rows - filterSize + 1;
            int zcols = x.cols - filterSize + 1;
            // numFilters: number of output filters
            int numFilters = filterList.size();
            LOG.fine("inList size: " + numIn + ", numFilters: " + numFilters);
            //

            for (int i = 0; i < numFilters; i++) {
                List<Matrix> filterRow = filterList.get(i);
                Matrix sumZ = new Matrix(zrows, zcols);
                // inList size = nIn
                for (int j = 0; j < nIn; j++) {
                    x = inList.get(j);
                    Matrix f = filterRow.get(j);
                    // find matrix convolution of x with filter f
                    // z = x convolve f
                    x.checkNaN("ConvoLayer x before convolve");
                    Matrix z = MTX.convolve(x, f);
                    //     LOG.finer("filter: " + filter);
                    //     LOG.finer("convo z: " + z);
                    MTX.normalizeInPlace(z);
                    // add convolved matrix z to sum
                    MTX.addInplace(sumZ, z);
                }
                // create output matrix yOut: apply activation function to matrix z
                Matrix y = actFn.trainingFn(sumZ);
                y.checkNaN("ConvoLayer sumZ");
                MTX.normalizeInPlace(y);
                //
                // number of matrix y in the outList = numFilters
                outList.add(y);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        LOG.fine("outList size: " + outList.size());
        return outList;
    }

    /**
     * Testing phase has no back propagation, use testing activation function
     * Perform forward propagation for this network layer
     *
     * @param inList input to this layer
     * @return matrix output y from this layer
     */
    public List<Matrix> testForward(List<Matrix> inList) {
        this.inList = inList;
        LOG.fine("trainForward");
        List<Matrix> outList = new ArrayList<>();
        try {
            // convolve matrix x with each filter f in filterList to create matrix z
            // matrix z: number of rows = x #rows - f #rows + 1
            // matrix z: number of cols = x #cols - f # cols + 1
            // output matrix y: z #rows, z #cols
            //
            // numIn: number of matrices to convolve
            int numIn = inList.size();
            // first x matrix for size
            Matrix x = inList.get(0);
            int zrows = x.rows - filterSize + 1;
            int zcols = x.cols - filterSize + 1;
            // numFilters: number of output filters
            int numFilters = filterList.size();
            LOG.fine("inList size: " + numIn + ", numFilters: " + numFilters);
            //
            // outList size = numFilters
            for (int i = 0; i < numFilters; i++) {
                List<Matrix> filterRow = filterList.get(i);
                Matrix sumZ = new Matrix(zrows, zcols);
                // inList size = nIn
                for (int j = 0; j < nIn; j++) {
                    x = inList.get(j);
                    Matrix f = filterRow.get(j);
                    // find matrix convolution of x with filter f
                    // z = x convolve f
                    x.checkNaN("ConvoLayer x before convolve");
                    Matrix z = MTX.convolve(x, f);
                    //     LOG.finer("filter: " + filter);
                    //     LOG.finer("convo z: " + z);
                    MTX.normalizeInPlace(z);
                    // add convolved matrix z to sum
                    MTX.addInplace(sumZ, z);
                }
                // create output matrix yOut: apply activation function to matrix z
                Matrix y = actFn.testingFn(sumZ);
                //
                // number of matrix y in the outList = numFilters
                outList.add(y);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        LOG.fine("outList size: " + outList.size());
        return outList;
    }

    /**
     * Perform backward propagation for convolution network layer
     * The weight matrix is the filter matrix
     *
     * @param dLdYList derivative of loss function with respect to layer output y
     * @return derivative of layer output loss with respect to layer input dL/dX
     */
    public List<Matrix> backProp(List<Matrix> dLdYList) {

        LOG.fine(" convo layer dLdYList size: " + dLdYList.size());
        // output dLdX: d(loss)/d(input x)
        // derivative of layer output loss with respect to layer input
        // There will be a dLdX(nf, nf) matrix for each input matrix
        List<Matrix> dLdXList = new ArrayList<>();
        try {
            // let n = number output nodes, m = number of input nodes
            // dLdX = dLdY * dYdX
            // dYdX = dYdZ * dZdX
            // dZdX = dZdf * dfdX
            int numFilters = filterList.size();
            // padding is 1 less than tha filter size
            int padSize = filterSize - 1;
            LOG.fine("padSize : " + padSize + ", numFilters: " + numFilters);
            // first x matrix for size
            Matrix x = inList.get(0);
            Matrix sumdLdX = new Matrix(x.rows, x.cols);
            LOG.fine("nIn : " + nIn + ", inList size: " + inList.size());
            LOG.fine( "filterList size: " + filterList.size());
            LOG.fine( "velocityList size: " + velocityList.size());
            for (int j = 0; j < nIn; j++) {
                x = inList.get(j);
                //
                for (int i = 0; i < numFilters; i++) {
                    List<Matrix> filterRow = filterList.get(i);
                    List<Matrix> velocityRow = velocityList.get(i);
                    LOG.fine( "filterRow size: " + filterRow.size());
                    LOG.fine( "velocityRow size: " + velocityRow.size());
                    Matrix f = filterRow.get(j);
                    // each filter matrix has an associated velocity matrix
                    Matrix v = velocityRow.get(j);
                    //
                    Matrix dLdY = dLdYList.get(i);
                    LOG.fine("dLdY : " + dLdY);
                    Matrix dLdYpad = MTX.copyAndPad(dLdY, padSize);

                    // dYdZ(n,1)
                    Matrix dYdZ = actFn.derivative();
                    // dLdZ(1,n) = dLdY(1,n) ** dYdZ(n,1), cell multiply
                    Matrix dLdZ = MTX.cellMult(dLdY, dYdZ);
                    LOG.fine("dYdZ : " + dYdZ);
                    LOG.fine("dLdZ : " + dLdZ);
                    //
                    LOG.fine("f : " + f);
                    // use f before update with df
                    Matrix rotatedF = MTX.rotate(f);
                    LOG.fine("rotatedF : " + rotatedF);
                    LOG.fine("dLdYpad : " + dLdYpad);
                    Matrix dLdX = MTX.convolve(dLdYpad, rotatedF);
                    LOG.fine("dLdX : " + dLdX);
                    MTX.addInplace(sumdLdX, dLdX);
                    //
                    // dLdf(),   dZdf(m,m) = x
                    //   Matrix dLdf = dLdZ.convolve(dZdf);
                    //     Matrix dLdf = MTX.convolve(x, dLdY);

                    // dLdf = dZdf  convolve dLdY
                    // dLdf = x convolve dLdY
                    Matrix dLdf = MTX.convolve(x, dLdY);
                    MathUtil.updateWeightMatrix(dLdf, eta, f, v, mu, oneMinusLambda);
                    /*
                    // change in filter, df = eta*dLdf
                    // df(f,f)
                    Matrix df = MTX.mulConstant(dLdf, -eta);
                    // update velocity matrix, initially zero
                    MTX.mulConstInPlace(v, mu);
                    // update velocity matrix with gradient
                    // v = mu * v - eta * dLdF
                    MTX.addInplace(v, df);
                    // L2 regularization to reduce weights on back prop
                    // f = f * (1 - lambda)
                    MTX.mulConstInPlace(f, oneMinusLambda);
                    // update filter matrix
                    // f = f - eta*dLdf
                    // f(f,f)
                    MTX.addInplace(f, v);

                     */
                }
                LOG.fine("sumdLdX : " + sumdLdX);
                // append convolution matrix dLdX sum to dLdX list
                dLdXList.add(sumdLdX);
            }
            LOG.fine("convo layer dLdXList size: " + dLdXList.size());
            //
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return dLdXList;
    }

} // end class