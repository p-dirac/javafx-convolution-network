package datasci.backend.control;

/*******************************************************************************
 *
 * Copyright 2022 Ronald Cook
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **************************************************************************/

import datasci.backend.activations.ActivationFactory;
import datasci.backend.activations.ActivationI;
import datasci.backend.layers.ConvoLayer;
import datasci.backend.layers.ConvoPoolLayer;
import datasci.backend.layers.InternalLayer;
import datasci.backend.layers.OutputLayer;
import datasci.backend.layers.PoolLayer;
import datasci.backend.model.ConvoConfig;
import datasci.backend.model.ConvoPoolConfig;
import datasci.backend.model.ConvoPoolFitParams;
import datasci.backend.model.EvaluationR;
import datasci.backend.model.InternalConfig;
import datasci.backend.model.InternalFitParams;
import datasci.backend.model.MTX;
import datasci.backend.model.MathUtil;
import datasci.backend.model.Matrix;
import datasci.backend.model.NetConfig;
import datasci.backend.model.NetResult;
import datasci.backend.model.OutputConfig;
import datasci.backend.model.OutputFitParams;
import datasci.backend.model.PoolConfig;
import datasci.backend.model.FitParams;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for convolution network for image classification on MNIST dataset
 * <p>
 * ref: https://www.analyticsvidhya.com/blog/2020/02/mathematics-behind-convolutional-neural-network/
 * <p>
 * Notation for each layer:
 * X = input matrix (e.g. matrix of image pixel values)
 * W = weight matrix
 * B = bias matrix
 * Z = W * X + B
 * S = activation function (e.g. sigmoid, RELU, softmax)
 * Y = S(Z)
 * Y = output matrix
 * L = loss function (e.g. sum square error, log likelihood)
 * <p>
 * <p>
 * Notes:
 * (Rows, columns) of input image = (n, n), n = image size
 * (Rows, columns) of filter matrix = (f,f), f = filter size
 * let nf = n - f + 1
 * (Rows, columns) of convolution output matrix = ( nf , nf )
 * (Rows, columns) of pool matrix = (p,p)
 * let nfp = (n-f+1) / p = nf / p
 * (Rows, columns) of pool output matrix = ( nfp , nfp )
 * number of pool output nodes: nfp * nfp * numFilters = pOut
 * (Rows, columns) of internal layer input matrix = (pOut,1)
 * (Rows, columns) of internal layer output matrix = (iOut, 1)
 * (Rows, columns) of output layer input matrix = (iOut, 1)
 * (Rows, columns) of output layer output matrix = (oOut, 1)
 *
 * <p>
 * Example:
 * input image = (28, 28)
 * filter matrix = (5,5)
 * nf = 28 - 5 + 1 = 24, convolution output matrix
 * convolution output matrix = (24 , 24)
 * numFilters = 20
 * pool matrix = (2,2)
 * nfp = nf/2 = 24/2 = 12, pool output matrix size
 * pool output matrix = (12 , 12)
 * pool output nodes, pOut = 12 * 12 * 20 = 2880
 * internal layer input matrix, (pOut,1) = (2880, 1)
 * internal layer output matrix, (iOut, 1) = (500, 1)
 * output layer input matrix, (iOut, 1) = (500, 1)
 * output layer output matrix, (oOut, 1) = (10, 1)
 * <p>
 * <p>
 * <p>
 * Objective: iterate over multiple inputs, update weight and bias to minimize the loss
 * predicted y = softmax(z)
 * column matrix loss, dLdZ[k] = pred y[k] -  actual y[k]
 * dL/dW = change in loss due to change in weight
 * dL/dB = change in loss due to change in bias
 * eta = gradient descent rate
 * Weight correction at each iteration from i to i+1:
 * W(i + 1) = W(i) - eta*(dL/dW)
 * Bias correction at each iteration from i to i+1:
 * B(i + 1) = B(i) - eta*(dL/dB)
 * How to calculate dL/dW?
 * dL/dW = (dL/dY)*(dY/dZ)*(dZ/dW)
 * How to calculate dL/dB?
 * dL/dB = (dL/dY)*(dY/dZ)*(dZ/dB)
 * Find dL/dX for back propagation:
 * dL/dX = (dL/dY)*(dY/dZ)*(dZ/dX)
 * How to back propagate to previous layer?
 * dL/dY previous layer = dL/dX current layer,
 * then apply dL/dW calculation as above to update weight and bias in previous layer,
 * then calculate dL/dX in previous layer for back prop input (dL/dY) to next previous layer
 *
 * @author cook
 */
public class ConvoNetBase {


    private static final Logger LOG = Logger.getLogger(ConvoNetBase.class.getName());
    private static final Logger BACKEND_LOGGER = Logger.getLogger("datasci.backend");

    //
    // number of data classes (labels): e.g. 10 for 0 to 9 digits classification
    protected int numOutputNodes;
    //
    // private ConfigProps props;
    protected NetConfig config;
    //
    // network layers
    protected List<ConvoPoolLayer> convoPoolLayers;
    protected List<InternalLayer> internalLayers;
    protected ConvoLayer convoLayer;
    protected PoolLayer poolLayer;
    protected InternalLayer internalLayer;
    protected OutputLayer outputLayer;
    //
    // network counts
    protected int sampleCount;
    protected int numCorrect;
    protected int batchSampleCount;
    protected int batchNumCorrect;
    //
    // NetResult will hold the overall summary of network performance, and also the
    // network training FitParams
    protected NetResult netResult;
    protected String status;
    //

    /**
     * Instantiates a new Convolution network.
     */
    public ConvoNetBase(NetResult netResult) {
        this.netResult = netResult;
        initcounts();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void initcounts(){
        sampleCount = 0;
        numCorrect = 0;
        batchSampleCount = 0;
        batchNumCorrect = 0;
    }
    /**
     * Create network configuration:
     * creates all network layers,
     * but does not initialize any parameters such as weights
     */
    public void configureNet(NetConfig config) {
        try {
            this.config = config;
            LOG.info("Network configuration");
            //
            int imageW = config.inputConfig.cols;
            int imageH = config.inputConfig.rows;
            LOG.info("imageW: " + imageW + ", imageH: " + imageH);
            //
            List<ConvoPoolConfig> convoPoolList = config.convoPoolList;
            List<InternalConfig> internalList = config.internalList;
            OutputConfig outputConfig = config.outputConfig;
            //
            convoPoolLayers = new ArrayList<>();
            //
            // assume all square matrix
            //
            // matrix size is number of rows (= number of columns)
            int convoInMatrixSize = imageH;
            // convoInListSize: list size is number of input matrix into convo layer
            int convoInListSize = 1;
            int convoOutMatrixSize = 0;
            //
            int poolInListSize = 1;
            int poolInMatrixSize = 0;
            int poolOutMatrixSize = 0;
            int poolOutNodes = 0;
            //
            int internalOutNodes = 0;
            // in case there are no convo pool layers
            int internalInNodes = imageH * imageH;
            //
            // in case there are no convo pool or internal layers
            int outputInNodes = imageH * imageH;
            //
            if(convoPoolList.size() > 0) {
                int convoID = 0;
                int poolID = 0;
                for (ConvoPoolConfig convoPool : convoPoolList) {
                    ConvoConfig convoConfig = convoPool.convoConfig;
                    ConvoPoolLayer convoPoolLayer = new ConvoPoolLayer();
                    ActivationI actFn = ActivationFactory.getActivation(convoConfig.actName);
                    int filterSize = convoConfig.filterSize;
                    int numFilters = convoConfig.numFilters;
                    LOG.info("filterSize: " + filterSize + ", numFilters: " + numFilters +
                    ", convoInListSize: " + convoInListSize);
                    convoLayer = new ConvoLayer(convoInListSize, numFilters, actFn, filterSize);
                    convoLayer.setnOut(numFilters);
                    convoLayer.setLayerID("convo." + convoID);
                    // nf = n - f + 1
                    convoOutMatrixSize = convoInMatrixSize - filterSize + 1;
                    LOG.info("convoInMatrixSize: " + convoInMatrixSize + ", convoOutMatrixSize: " + convoOutMatrixSize);
                    convoPoolLayer.convoLayer = convoLayer;
                    //
                    // pool input list size = pool output list size
                    poolInListSize = numFilters;
                    PoolConfig poolConfig = convoPool.poolConfig;
                    int poolSize = poolConfig.poolSize;
                    LOG.info("poolSize: " + poolSize + ", numFilters: " + numFilters + ", poolInListSize: " + poolInListSize);
                    poolLayer = new PoolLayer(poolConfig.poolSize);
                    poolLayer.setLayerID("pool." + poolID);
                    convoPoolLayer.poolLayer = poolLayer;
                    //
                    convoPoolLayers.add(convoPoolLayer);
                    //
                    poolInMatrixSize = convoOutMatrixSize;
                    // nfp = nf / p
                    poolOutMatrixSize = convoOutMatrixSize / poolSize;
                    LOG.info(" poolInMatrixSize: " + poolInMatrixSize + ", poolOutMatrixSize: " + poolOutMatrixSize);
                    // pool output nodes: (pool input list size) * nfp * nfp
                    poolOutNodes = poolInListSize * poolOutMatrixSize * poolOutMatrixSize;
                    LOG.info("poolSize: " + poolSize + ", poolOutNodes: " + poolOutNodes);

                    // reset for next layer
                    convoInMatrixSize = poolOutMatrixSize;
                    // numFilters into convo = numFilters out of pool
                    convoInListSize = poolInListSize;
                    convoID++;
                    poolID++;
                }
                internalInNodes = poolOutNodes;
                outputInNodes = poolOutNodes;
            }
            //
            internalLayers = new ArrayList<>();
            //
            if(internalList.size() > 0) {
                internalOutNodes = 0;
                int internalID = 0;
                for (InternalConfig internalConfig : internalList) {
                    internalOutNodes = internalConfig.numOutputNodes;
                    LOG.info("internalInNodes: " + internalInNodes + ", internalOutNodes: " + internalOutNodes);
                    ActivationI actFn = ActivationFactory.getActivation(internalConfig.actName);
                    internalLayer = new InternalLayer(internalInNodes, internalOutNodes, actFn);
                    internalLayer.setLayerID("internal." + internalID);
                    internalLayers.add(internalLayer);
                    //reset for next layer
                    internalInNodes = internalOutNodes;
                    internalID++;
                }
                outputInNodes = internalOutNodes;
            }
            //
            int outputID = 0;
            ActivationI actFn = ActivationFactory.getActivation(outputConfig.actName);
            outputLayer = new OutputLayer(outputInNodes, outputConfig.numOutputNodes, actFn);
            outputLayer.setLayerID("output." + outputID);
            //
            // create confusion matrix of number correct along diagonal
            // off diagonal values represent number incorrect for that digit
            numOutputNodes = outputConfig.numOutputNodes;
            LOG.info("outputInNodes: " + outputInNodes + ", numOutputNodes: " + numOutputNodes);
            Matrix summaryResults = new Matrix(numOutputNodes, numOutputNodes);
            netResult.summaryResults = summaryResults;
            //
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Prepare list of actual column matrices for measuring network performance.
     * Each matrix has value 1.0 at the row index equal to the actual value.
     * For example, for actual value of 3, the 3rd matrix will have 1 in row 3.
     * All other cell values are zero.
     *
     * @return list of actual column matrix for measuring network performance.
     */
    public List<Matrix> prepActualMatrix() {
        List<Matrix> acutalList = new ArrayList<>();
        try {
            LOG.info("prepActualMatrix");
            for (int i = 0; i < numOutputNodes; i++) {
                // create column matrix
                Matrix actualMatrix = new Matrix(numOutputNodes, 1);
                // set ith row to value i, all other rows zero
                // zero based row, col index
                MTX.setCell(actualMatrix, i, 0, 1.0);
                acutalList.add(actualMatrix);
                LOG.info("i: " + i + ", actualMatrix: " + MathUtil.arraytoString(actualMatrix.a));
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return acutalList;
    }


    /**
     * Update evaluation counters
     *
     * @param actualIndex
     */
    public void updateEval(int actualIndex) {
        // compare actual class index to predicted class index
        boolean iscorrect = outputLayer.isCorrect(actualIndex);
        if (iscorrect) {
            // total number of samples with correct predictions
            numCorrect++;
            // number of samples in this batch with correct predictions
            batchNumCorrect++;
        }
        // class index for predicted output max value
        int predictedIndex = outputLayer.getPredictedIndex();
        netResult.updateSummary(actualIndex, predictedIndex);
    }

    /**
     * Check if output value is correct
     *
     * @return true if output value is correct
     */
    public EvaluationR evaluate() {
        EvaluationR eval = new EvaluationR(sampleCount, numCorrect, batchSampleCount, batchNumCorrect);
        return eval;
    }

    /**
     * Extract FitParams from layers and copy to netResult.
     * FitParams: all parameters required to forward propagate the layer
     * set all filters, weights, biases, sizes,
     * does not include any back propagation parameters, such as gradient descent rates
     *
     * @return the FitParams
     */
    public FitParams createFitParams() {
        FitParams fitParams = new FitParams();
        try {
            // convoPoolList size: # of convolution/pool layers
            List<ConvoPoolFitParams> convoPoolList = new ArrayList<>();
            for (ConvoPoolLayer convoPool : convoPoolLayers) {
                // convoPoolFitParams: fit parameters for one convolutional layer
                // filterList:
                //    outer list size: # output feature maps;
                //    inner list size: # input feature maps
                ConvoPoolFitParams convoPoolFitParams = new ConvoPoolFitParams();
                convoPoolFitParams.layerID = convoPool.convoLayer.getLayerID();
                convoPoolFitParams.filterList = convoPool.convoLayer.getFilterList();
                convoPoolFitParams.bias = convoPool.convoLayer.getBias();
                convoPoolList.add(convoPoolFitParams);
            }
            //
            // internalList size: # of internal layers
            List<InternalFitParams> internalList = new ArrayList<>();
            for (InternalLayer internal : internalLayers) {
                InternalFitParams internalFitParams = new InternalFitParams();
                internalFitParams.layerID = internal.getLayerID();
                internalFitParams.w = internal.getW();
                internalFitParams.b = internal.getB();
                internalList.add(internalFitParams);
            }
            //
            OutputFitParams outputFitParams = new OutputFitParams();
            outputFitParams.w = outputLayer.getW();
            outputFitParams.b = outputLayer.getB();
            //

            //
            fitParams.convoPoolList = convoPoolList;
            //
            fitParams.internalList = internalList;
            //
            fitParams.outputFitParams = outputFitParams;
            //

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return fitParams;
    }

    /**
     * For each layer, set the FitParams: all parameters required to forward propagate the layer
     * set all filters, weights, biases, sizes,
     * does not include any back propagation parameters, such as gradient descent rates
     */
    public void setFitParams(FitParams fitParams) {
        try {
            List<ConvoPoolFitParams> convoPoolList = fitParams.convoPoolList;
            int convoPoolSize = convoPoolLayers.size();
            for (int i = 0; i < convoPoolSize; i++) {
                ConvoPoolLayer convoPool = convoPoolLayers.get(i);
                ConvoPoolFitParams convoPoolFitParams = convoPoolList.get(i);
                convoPool.convoLayer.setFilterList(convoPoolFitParams.filterList);
                convoPool.convoLayer.setBias(convoPoolFitParams.bias);
            }
            //
            List<InternalFitParams> internalList = fitParams.internalList;
            int internalSize = internalLayers.size();
            for (int k = 0; k < internalSize; k++) {
                InternalLayer internal = internalLayers.get(k);
                InternalFitParams internalFitParams = internalList.get(k);
                internal.setW(internalFitParams.w);
                internal.setB(internalFitParams.b);
            }
            //
            OutputFitParams outputFitParams = fitParams.outputFitParams;
            outputLayer.setW(outputFitParams.w);
            outputLayer.setB(outputFitParams.b);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get network results
     *
     * @return network results
     */
    public NetResult getNetResult() {
        return netResult;
    }

}   // end class
