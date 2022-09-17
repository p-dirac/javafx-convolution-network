package datasci.backend.layers;

import datasci.backend.activations.ActivationI;
import datasci.backend.model.ConvoNode;
import datasci.backend.model.MTX;
import datasci.backend.model.MathUtil;
import datasci.backend.model.Matrix;
import datasci.backend.model.MomentumNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 Network Convolution Layer
 reference:
 https://www.analyticsvidhya.com/blog/2020/02/mathematics-behind-convolutional-neural-network/
 for backprop pool layer: https://victorzhou.com/blog/intro-to-cnns-part-2/
 <p>
 Notes:
 (Rows, columns) of input matrix = (n, n)
 (Rows, columns) of filter matrix = (f,f)
 (Rows, columns) of convolution output matrix = ((n-f+1) , (n-f+1))

 //
 For the convolutional layer, the weight matrix is replaced by the filter matrix, and the
 matrix multiplication W*X is replaced by matrix convolution. See the references above on
 how matrix convolution and max pooling work.

 The general outline above shows the calculation steps for a single weight matrix. However,
 the convolutional layer usually contains many filter matrices. How does the calculation
 change for more than one filter matrix?

 Let's examine the Z function closely.
 Z = W * X + B
 The input matrix is the output Y from the previous layer. Consider the second convolutional
 layer which has for example 20 input feature maps coming from the first layer. In this case
 X means a column list of 20 matrices. What is W? The number of rows in W is the number of
 ouput nodes from the current layer. For example, if the second convolutional layer has 50
 output nodes, W has 50 rows, and 20 columns.

 Note well: each cell of W is a filter matrix (e.g 5x5), while each cell of X is a feature map
 matrix (e.g. 12x12). In this example, there would be 20*50 = 1000 matrix convolutions to
 produce Z. Therefore, Z is a column of 50 output feature map matrices (e.g. 8x8).
 As you can see in convoLayer.trainForward, the argument is List<Matrix>. Compare with
 internalLayer.trainForward, which takes a single Matrix argument for X.

 //
 In the convolutional layer, the weight W is the filter, and there is a bias B.
 Z = W * X + B, where W * X is matrix convolution
 We can use 'unfolded' matrix preparation to turn convolution into matrix multiplication.
 See MTX. Convolve, MTX.unfold, and MatrixTests.unfold for implementation details.
 dL/dZ = (dL/dY)*(dY/dZ)
 dL/dY : back prop input from previous layer
 dY/dZ = derivative of activation function
 dL/dX = (dL/dZ)*(dZ/dX)
 dZ/dX = W
 dL/dX = unfolded{(dL/dZ)} * rotated(W)
 dZ/dW = X
 dL/dW = unfolded{(dL/dZ)} * (dZ/dW)
 v(i + 1) = mu * v(i) - eta * batch ave[dL/dW]
 Weight correction after each batch from i to i+1:
 W(i + 1) = (1 - lambda)*W(i) + v(i + 1)
 For bias:
 dZ/dB = I
 dL/dB = (dL/dZ)*(dZ/dB) = (dL/dZ)
 Bias correction after each batch from i to i+1:
 B(i + 1) = B(i) + batch ave[dL/dB]

 //
 Reference:
 Convolution multi-node input:
 https://towardsdatascience.com/backpropagation-in-a-convolutional-layer-24c8d64d8509?gi=9e0f2eebeef5

 Convolution - back prop:
 https://bishwarup307.github.io/deep%20learning/convbackprop/
 //
 */
public class ConvoLayer {

    private static final Logger LOG = Logger.getLogger(ConvoLayer.class.getName());
    //
    private final LayerE layerType = LayerE.CONVO;
    // activation function and derivative
    private ActivationI actFn;
    // list of matrix is the input from previous layer
    private List<Matrix> inList;
    // list of unfolded input matrix for filter convolution
    private List<Matrix> unfoldList;
    // list of unfolded input matrix for dLdZ convolution
    private List<Matrix> unfoldFordLdZ;
    // number of inputs (matrices) to each node (number of nodes in previous layer)
    private int nIn;
    // number of output nodes (nOut: # output feature maps) in this layer
    // number of filters per input node
    // total number of filters: nOut * nIn
    private int nOut;

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
    // dLdXList: backprop derivative to pass to previous layer as dLdY
    private List<Matrix> dLdXList = new ArrayList<>();
    //
    //
    private int filterSize;
    // filterList:
    //    outer list size: # output feature maps (nOut)
    //    ConvoNode list size: # input feature maps (nIn)
    //    could be implemented as Matrix[nOut][nIn], but List does not need dimensions
    // total number of filters: nOut * nIn
    private List<ConvoNode> filterList = new ArrayList<>();
    // list to save activation function derivatives (dYdZ) for each output node during train forward
    private List<Matrix> derivList = new ArrayList<>();
    // bias matrix list
    // bias matrix: nOut rows, one column
    private Matrix bias;
    private int batchCount;
    // batchFilterUpdates: batch list of dLdW for back prop update to each filter
    // outer list is for each sample
    // inner list contains set of filter updates for one sample
    private List<List<ConvoNode>> batchFilterUpdates = new ArrayList<>();
    // batchBias: batch of dLdB for back prop update to each output node
    private Matrix batchBias;
    // velocity matrix: same size as filterList, nOut rows, nIn columns
    private List<MomentumNode> momentumList = new ArrayList<>();
    // to reproduce results, use same seed for output layer weights
    private static final long OUTPUT_WT_SEED = 4321;

    // standard deviation for initial weight
    private double stdDev;
    //
    private final Random filterRand = new Random(OUTPUT_WT_SEED);
    // ID for debug purposes
    private String layerID;
    // doNow flag for debug logging
    private boolean doNow;

    /**
     Instantiates a new Internal layer.
     */
    public ConvoLayer() {
    }

    /**
     Creates a new Convolution layer.

     @param actFn the activation function
     */
    public ConvoLayer(int nIn, int nOut, ActivationI actFn, int filterSize) {
        this.nIn = nIn;
        // nOut : number of filters per input node
        this.nOut = nOut;
        this.actFn = actFn;
        this.filterSize = filterSize;
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

    public List<ConvoNode> getFilterList() {
        return filterList;
    }

    public Matrix getBias() {
        return bias;
    }

    public void setBias(Matrix bias) {
        this.bias = bias;
    }

    public void setFilterList(List<ConvoNode> filterList) {
        this.filterList = filterList;
        this.nOut = filterList.size();
        initMomentumList(filterSize, nOut);
    }

    public int getnOut() {
        return nOut;
    }

    public void setnOut(int nOut) {
        this.nOut = nOut;
    }

    public int getFilterSize() {
        return filterSize;
    }

    public void setFilterSize(int filterSize) {
        this.filterSize = filterSize;
    }

    /**
     Init filter matrix
     Note: call this method if filterList is not input
     */
    public Matrix initFilter() {
        //    LOG.info("initFilter, rows: " + filterSize + ", cols: " + filterCols);
        Matrix w = new Matrix(filterSize, filterSize);
        //   LOG.info("filter : " + w);
        try {
            for (int k = 0; k < w.size; k++) {
                // initialize each weight to random gaussian with mean zero
                // scale the standard deviation by filter size
                w.a[k] = filterRand.nextGaussian() * stdDev;
            }
            //  LOG.info("w: " + w);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return w;
    }

    public void initFilterList(int filterSize, int nOut) {
        try {
            filterList.clear();
            this.filterSize = filterSize;
            LOG.info("filterSize: " + filterSize + ", nOut: " + nOut + ", nIn: " + nIn);
            // standard deviation for initial weight
            stdDev = Math.sqrt(2.0 / (filterSize * filterSize));
            LOG.info("stdDev: " + stdDev);
            // nOut: number of filter rows
            // filterList: one filter row = list of column filters
            for (int k = 0; k < nOut; k++) {
                // create one filter per input node
                ConvoNode convoNode = new ConvoNode();
                for (int i = 0; i < nIn; i++) {
                    //  LOG.info("i: " + i);
                    //   LOG.info("j: " + j + ", call initFilter");
                    Matrix f = initFilter();
                    //Replaces the element at the specified position in this list with the specified element
                    convoNode.add(f);
                }
                filterList.add(convoNode);
            }
            LOG.info("filterList size: " + filterList.size());
            initMomentumList(filterSize, nOut);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    public void initMomentumList(int filterSize, int nOut) {
        try {
            // momentumList: one velocity row = list of column velocities
            for (int k = 0; k < nOut; k++) {
                MomentumNode momentumNode = new MomentumNode();
                for (int i = 0; i < nIn; i++) {
                    Matrix v = new Matrix(filterSize, filterSize);
                    momentumNode.add(v);
                }
                momentumList.add(momentumNode);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Init b.
     */
    public void initBias(int nOut) {
        // bias matrix w will be matrix with nOut rows and 1 columns
        // default values = zero
        bias = new Matrix(nOut, 1);
    }

    /**
     Perform forward propagation for this network layer

     @param inList input to this layer; treat matrix x as rectangular, not a one column matrix
     @return list of matrix output y from this layer; treat matrix y as a rectangular matrix
     */
    public List<Matrix> trainForward(List<Matrix> inList) {
        this.inList = inList;
        // outList size = nOut
        // each y matrix (feature map) size:
        //    rows: x.rows - filterSize + 1;
        //    cols: x.cols - filterSize + 1;
        List<Matrix> outList = new ArrayList<>();
        try {
            // convolve matrix x with each filter w in filterList to create matrix z
            // matrix z: number of rows = x #rows - filterSize + 1
            // matrix z: number of cols = x #cols - filterSize + 1
            // output matrix y: z #rows, z #cols
            //
            // numIn: number of matrices to convolve
            int numIn = inList.size();
            // first x matrix for size
            Matrix x = inList.get(0);
            // feature map size, i.e. convolved matrix size
            int zrows = x.rows - filterSize + 1;
            int zcols = x.cols - filterSize + 1;
            // nOut: number of output feature maps
            //
            // initialize the unfolded x
            unfoldList = new ArrayList<>();
            unfoldFordLdZ = new ArrayList<>();
            for (int i = 0; i < nIn; i++) {
                x = inList.get(i);
                // matrix xu size: # rows = zrows * zcols, # cols = filterSize * filterSize
                Matrix xu = MTX.unfold(x, filterSize, filterSize);
                unfoldList.add(xu);
                // matrix xu size: # rows = zrows * zcols, # cols = filterSize * filterSize
                Matrix xuFordLdZ = MTX.unfold(x, zrows, zcols);
                unfoldFordLdZ.add(xuFordLdZ);
            }
            //
            for (int k = 0; k < nOut; k++) {
                // get next row of filters
                ConvoNode filterRow = filterList.get(k);
                double b = bias.a[k];
                // sumZ: sum of convolutions over nIn for one output node
                Matrix sumZ = new Matrix(zrows, zcols);
                // inList size = nIn
                for (int i = 0; i < nIn; i++) {
                    Matrix w = filterRow.get(i);
                    // matrix xu size: # rows = zrows * zcols, # cols = filterSize * filterSize
                    Matrix xu = unfoldList.get(i);
                    // flatten w for unfolded convolve
                    w.cols = 1;
                    w.rows = w.size;
                    //
                    // matrix z size: # rows = zrows * zcols, # cols = 1
                    Matrix z = MTX.mult(xu, w);

                    //    LOG.log(Level.INFO, "z: " + z );
                    // reset z size to match sumZ matrix
                    z.rows = zrows;
                    z.cols = zcols;
                    z.size = z.rows * z.cols;
                    // add convolved matrix z to sum
                    MTX.addInplace(sumZ, z);
                }
                //
                sumZ.checkNaN("ConvoLayer ID: " + layerID + ", fore sumZ");
                // add bias
                MTX.addConstantInPlace(sumZ, b);
                // create output matrix yOut: apply activation function to matrix z
                Matrix y = actFn.trainingFn(sumZ);
                // activation function derivative (dYdZ)
                Matrix dYdZ = actFn.derivative();
                derivList.add(dYdZ);
                //
                // number of matrix y (feature map) in the outList = nOut
                // each y matrix size: (zrows, zcols)
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
     Testing phase has no back propagation, use testing activation function
     Perform forward propagation for this network layer

     @param inList input to this layer
     @return matrix output y from this layer
     */
    public List<Matrix> testForward(List<Matrix> inList) {
        this.inList = inList;
        LOG.fine("trainForward");
        List<Matrix> outList = new ArrayList<>();
        try {
            // convolve matrix x with each filter w in filterList to create matrix z
            // matrix z: number of rows = x #rows - w #rows + 1
            // matrix z: number of cols = x #cols - w # cols + 1
            // output matrix y: z #rows, z #cols
            //
            // numIn: number of matrices to convolve
            int numIn = inList.size();
            // first x matrix for size
            Matrix x = inList.get(0);
            int zrows = x.rows - filterSize + 1;
            int zcols = x.cols - filterSize + 1;
            //
            // outList size = nOut
            for (int k = 0; k < nOut; k++) {
                // get next row of filters
                ConvoNode filterRow = filterList.get(k);
                double b = bias.a[k];
                // sumZ: sum of convolutions over nIn for one output node
                Matrix sumZ = new Matrix(zrows, zcols);
                // inList size = nIn
                for (int i = 0; i < nIn; i++) {
                    Matrix w = filterRow.get(i);
                    // matrix xu size: # rows = zrows * zcols, # cols = filterSize * filterSize
                    Matrix xu = unfoldList.get(i);
                    // flatten f for unfolded convolve
                    w.cols = 1;
                    w.rows = w.size;
                    //
                    // matrix z size: # rows = zrows * zcols, # cols = 1
                    Matrix z = MTX.mult(xu, w);
                    // reset z size to match sumZ matrix
                    z.rows = zrows;
                    z.cols = zcols;
                    z.size = z.rows * z.cols;
                    // add convolved matrix z to sum
                    MTX.addInplace(sumZ, z);
                }
                //
                sumZ.checkNaN("ConvoLayer ID: " + layerID + ", fore sumZ");
                // add bias
                MTX.addConstantInPlace(sumZ, b);
                // create output matrix yOut: apply activation function to matrix z
                Matrix y = actFn.testingFn(sumZ);
                //
                // number of matrix y (feature map) in the outList = nOut
                // each y matrix size: (zrows, zcols)
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
     Perform backward propagation for convolution network layer
     The weight matrix is the filter matrix
     backProp is called for each sample within a batch
     Weights and biases are updated after each complete batch

     @param dLdYList derivative of loss function with respect to layer output y
     @return derivative of layer output loss with respect to layer input dL/dX
     */
    public List<Matrix> backProp(List<Matrix> dLdYList, boolean batchCompleted) {
        // output dLdX: d(loss)/d(input x)
        // derivative of layer output loss with respect to layer input
        // There will be a dLdX(nf, nf) matrix for each output node
        List<Matrix> dLdXList = new ArrayList<>();
        try {
            // let nOut = number output nodes, nIn = number of input nodes
            //
            // each output y matrix (feature map) size:
            //  dLdZ  rows: x.rows - filterSize + 1;
            //  dLdZ  cols: x.cols - filterSize + 1;
            //
            // dLdX = (dLdY * dYdZ) * dZdX
            // dZdX = W (size f,f)
            // dLdX = (dLdY * dYdZ) * W
            // dLdX = (dLdZ) * W
            // padding is 1 less than tha filter size
            int padSize = filterSize - 1;
            // first x matrix for sizing later on
            Matrix x = inList.get(0);
            //
            //
            if(batchCount == 0){
                batchBias = new Matrix(nOut, 1);
            }

            //
            Matrix dLdX = null;
            // dLdW for each filter, total of nOut*nIn filters
            List<ConvoNode> dLdWList = new ArrayList<>();
            //
            for (int k = 0; k < nOut; k++) {
                ConvoNode filterRow = filterList.get(k);
                // dLdWRow contains nIn filters
                // there is one dLdWRow for each output node
                ConvoNode dLdWRow = new ConvoNode();
                //
                // dLdY size:
                Matrix dLdY = dLdYList.get(k);
                // dYdZ(n,1) : n = nOut
                Matrix dYdZ = derivList.get(k);
                // dLdY, dYdZ, dLdZ : output feature map size
                //  dLdZ  rows: x.rows - filterSize + 1;
                //  dLdZ  cols: x.cols - filterSize + 1;
                Matrix dLdZ = MTX.cellMult(dLdY, dYdZ);
                // add padding to dLdZ so we don't lose information on the matrix edges
                // when we convolve the matrix
                Matrix dLdZpad = MTX.copyAndPad(dLdZ, padSize);
                // unfold dLdYpad for unfolded convolve with rotatedF
                Matrix dLdZpadU = MTX.unfold(dLdZpad, filterSize, filterSize);
                // flatten dLdZ for unfolded convolve
                dLdZ.rows = dLdZ.size;
                dLdZ.cols = 1;
                // dZdB = I (as column of 1.0)
                // dLdB = dLdZ * dZdB =  sum cells of dLdZ
                double dLdBcell = MTX.sumCells(dLdZ);
                // each output node has a bias (one bias for each filter row)
             //   dLdB.a[k] = dLdBcell;
                batchBias.a[k] += dLdBcell;
                //
                for (int i = 0; i < nIn; i++) {
                    //x = inList.get(j);
                    Matrix w = filterRow.get(i);
                    // use f before update with df
                    Matrix rotatedF = MTX.rotate(w);
                    // reset rotatedF size for unfolded convolve
                    rotatedF.cols = 1;
                    rotatedF.rows = rotatedF.size;
                    // dLdX = dLdY * dYdX
                    // dYdX = w
                    // unfolded convolve
                    dLdX = MTX.mult(dLdZpadU, rotatedF);
                    // reset dLdX size to match sumdLdX matrix
                    dLdX.rows = x.rows;
                    dLdX.cols = x.cols;
                    dLdX.size = x.rows * x.cols;
                    //
                    //  unfoldList created in trainForward
                    // matrix xu size: # rows = filterSize * filterSize,  # cols = zrows * zcols
                    Matrix xuFordLdZ = unfoldFordLdZ.get(i);
                    //
                    // dLdW = x convolve dLdZ
                    // dLdW = (dLdY * dYdZ) * dZdW
                    // dLdW = dLdZ * xu
                    // unfolded convolve
                    // dLdZ size: #rows = nOut, 1 col,
                    // matrix xu size: # rows = filterSize * filterSize,  # cols = zrows * zcols
                    // dLdZ : output feature map size
                    //  dLdZ  rows: x.rows - filterSize + 1;
                    //  dLdZ  cols: x.cols - filterSize + 1;
                    // dLdW size: filterSize * filterSize  rows, 1 col
                    Matrix dLdW = MTX.mult(xuFordLdZ, dLdZ);
                    //
                    // reset dLdW size to match filter matrix
                    dLdW.rows = filterSize;
                    dLdW.cols = filterSize;
                    dLdW.size = dLdW.rows * dLdW.cols;
                    dLdWRow.add(dLdW);
                //    MTX.logNotZero("ConvoLayer ID: " + layerID + ", batchCount: " + batchCount + " dLdW: ", dLdW);

                    //
                    // append convolution matrix dLdX to dLdX list
                    dLdXList.add(dLdX);
                }  // end nIn loop
                //
                // dldW for each filter, total of nOut*nIn filters
                // dLdWList contains nOut*nIn dLdW matrices
                dLdWList.add(dLdWRow);

                //     LOG.log(Level.INFO, "ConvoLayer ID: " + layerID + "batchCount: " + batchCount);
                //     LOG.log(Level.INFO, "dLdWRow: " + dLdWRow);

            }// end nOut loop

            //
            // for each batch sample, save whole set of dLdW for all output nodes
            // dLdWList contains nOut*nIn dLdW matrices
            batchFilterUpdates.add(dLdWList);
           //     LOG.log(Level.INFO, "batchCount: " + batchCount);
           //     LOG.log(Level.INFO, "dLdWRow: " + dLdWRow);

            //
            // ---------------------------------------------------------
            if (batchCompleted) {
                // sum each matrix cell from batchFilterUpdates list
                // dLdWSum: one ConvoNode for each output node
                List<ConvoNode> dLdWSumList = MTX.sumOfBatchNestedList(batchFilterUpdates);
            //    LOG.log(Level.INFO, "batchCount: " + batchCount);
            //    LOG.log(Level.INFO, "batchSumList: " + batchSumList);
                //
                double inv = 1.0 / batchCount;
                // loop over number of output nodes, nOut
                for (int k = 0; k < nOut; k++) {
                    ConvoNode filterRow = filterList.get(k);
                    // sum for this output node
                    ConvoNode dLdWSumNode = dLdWSumList.get(k);
                    //
                    MomentumNode momentumRow = momentumList.get(k);
                    //
                    //
                    // loop over number of input nodes, nIn
                    for (int i = 0; i < nIn; i++) {
                        Matrix dLdWSum = dLdWSumNode.get(i);
                        // average of dLdW over sample batch
                        Matrix avgdLdW = MTX.mulConstant(dLdWSum, inv);
                  //      LOG.log(Level.INFO, "ConvoLayer ID: " + layerID + ", batchCount: " + batchCount);
                  //      LOG.log(Level.INFO, "avgdLdW: " + avgdLdW);
                        //x = inList.get(j);
                        Matrix w = filterRow.get(i);
                        w.checkNaN("ConvoLayer ID: " + layerID + ", batch w");
                        // each filter matrix has an associated velocity matrix
                        Matrix v = momentumRow.get(i);
                        //
                        MathUtil.updateWeightMatrix(avgdLdW, eta, w, v, mu, oneMinusLambda);
                    }
                }
            }

            //
            //
            if (batchCompleted) {
                double inv = 1.0 / batchCount;
                // average of dLdB over batch count
                // avgdLdB: one row per output node, one column
                Matrix avgdLdB = MTX.mulConstant(batchBias, inv);
                // db(n, 1) = dLdB(n,1)*(-eta)
                Matrix dB = MTX.mulConstant(avgdLdB, -eta);
           //     LOG.log(Level.INFO, "batchCount: " + batchCount);
           //     LOG.log(Level.INFO, "dB: " + dB);

                // bias(n, 1) = bias(n, 1) + db(n, 1)
                // bias: nOut rows, one column
                MTX.addInplace(bias, dB);
                //
                batchCount = 0;
                batchFilterUpdates.clear();
            } else {
                //
                batchCount++;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return dLdXList;
    }


} // end class