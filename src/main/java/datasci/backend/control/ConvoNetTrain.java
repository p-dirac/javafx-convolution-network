package datasci.backend.control;

/*

  Copyright 2022 Ronald Cook

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

import datasci.backend.layers.ConvoLayer;
import datasci.backend.layers.ConvoPoolLayer;
import datasci.backend.layers.InternalLayer;
import datasci.backend.layers.PoolLayer;
import datasci.backend.model.ImageDataUtil;
import datasci.backend.model.MTX;
import datasci.backend.model.MathUtil;
import datasci.backend.model.Matrix;
import datasci.backend.model.NetConfig;
import datasci.backend.model.NetData;
import datasci.backend.model.NetResult;
import datasci.backend.model.EtaModel;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 Implementation of convolution network for image classification on MNIST dataset

 References:
 https://www.analyticsvidhya.com/blog/2020/02/mathematics-behind-convolutional-neural-network/

 https://towardsdatascience.com/deriving-the-backpropagation-equations-from-scratch-part-2-693d4162e779

 eta triangle function:
 https://www.jeremyjordan.me/nn-learning-rate/

 png image format used by this application was downloaded from:
 https://github.com/myleott/mnist_png

 // ------------
 Model Notation for each layer:
 X = input matrix (e.g. matrix of image pixel values)
 W = weight matrix
 B = bias matrix
 Z = W * X + B
 S = activation function (e.g. sigmoid, RELU, softmax)
 Y = S(Z)
 Y = output matrix
 L = loss function (e.g. sum square error, log likelihood)
 nIn = number of input nodes into a layer
 nOut = number of output nodes from a layer

 The weights and biases must be initialized before running the network.
 The standard approach is to use a random gaussian distribution with mean
 zero and standard devitaion related to the layer input size. See the
 initWeight method in each layer for implementation details.

 Note: for convolution layer, the weight matrix W is replaced with the
 filter matrix, and matrix multiplication is replaced by matrix convolution.

 // ------------
 Matrix sizing:
 Input Layer:
 (Rows, columns) of input image = (n, n)
 n = image size
 Convolution Layer:
 filter matrix = (f,f),
 f = filter size
 let nf = n - f + 1 = = size of output feature map
 convolution output matrix = ( nf , nf )
 nOut = number of output feature maps
 Pool Layer:
 pool matrix = (p,p)
 let nfp = (n-f+1) / p = nf / p
 pool output matrix = ( nfp , nfp )
 number of pool output nodes:
 pOut = nfp * nfp * nOut
 Internal Layer:
 internal layer input matrix = (pOut,1)
 internal layer output matrix = (iOut, 1)
 Output Layer:
 "output layer" input matrix = (iOut, 1)
 "output layer" output matrix = (oOut, 1)

 // ------------
 Example:
 Input Layer:
 input image = (28, 28)
 //
 1st Convolution Layer:
 input: one image matrix(28,28), nIn = 1
 filter matrix = (5,5), f= 5
 nf = 28 - 5 + 1 = 24, size of feature map
 convolution output matrix = (24 , 24)
 nOut = 20, number of output feature maps
 //
 1st Pool Layer:
 pool matrix = (2,2)
 nfp = nf/2 = 24/2 = 12,
 pool output matrix = (12 , 12)
 pool output nodes, pOut = 12 * 12 * 20 = 2880
 //
 2nd Convolution/Pool Layer:
 input: 20 pool matrices(12,12), nIn = 20
 filter matrix = (5,5), f= 5
 nf = 12 - 5 + 1 = 8, size of feature map
 convolution output matrix = (8 , 8)
 nOut = 50, number of output feature maps
 //
 2nd Pool Layer:
 pool matrix = (2,2)
 nfp = nf/2 = 8/2 = 4,
 pool output matrix = (4 , 4)
 pool output nodes, pOut = 4 * 4 * 50 = 800
 //
 Internal Layer:
 internal layer input matrix, (pOut,1) = (800, 1)
 internal layer output matrix, (iOut, 1) = (500, 1)
 //
 Output Layer:
 output layer input matrix, (iOut, 1) = (500, 1)
 output layer output matrix, (oOut, 1) = (10, 1)

 // ------------
 Objective: iterate over multiple inputs, update weight and bias to minimize the loss.
 Iteration: processing one input sample, such as an image.
 Batch: a small number of iterations through the forward propagation and back propagation
 before performing updates on the weights and biases. The purpoase of the batch is to
 smooth out the updates. The batch size is normally about 20 iterations, so if there are
 20,000 samples and 20,000 iterations, the number of batches would be 1000. A batch size
 of 1 means updates are performed on every iteration.

 predicted y = softmax(z)
 column vector loss, dLdZ[k] = (predicted y[k]) -  (actual y[k])
 where k is the class index (0 to 9 for digit classification)
 //
 dL/dW = change in loss due to change in weight
 batch ave[dL/dW] = average of dL/dW over batch size
 dL/dB = change in loss due to change in bias
 batch ave[dL/dB] = average of dL/dB over batch size
 //
 eta = gradient descent rate
 //
 momentum v: exponential average of the gradient steps
 mu: momentum factor, close to 1, usually about 0.9
 v(i + 1) = mu * v(i) - eta * batch ave[dL/dW]
//
 L2 regularization factor, lambda: small number which reduces the weight
 matrix on each update.
 //
 Weight correction at each iteration from i to i+1:
 W(i + 1) = (1 - lambda)*W(i) + v(i + 1)
 //
 Bias correction at each iteration from i to i+1:
 B(i + 1) = B(i) – eta * batch ave[dL/dB]
 //
 How to calculate dL/dW?
 dL/dW = (dL/dY)*(dY/dZ)*(dZ/dW)
 Recall Z = W*X + B
 Recall Y = S(Z) where S(Z) is the activation function
 dY/dZ depends on which activation function is applied
 dZ/dW = X
 How to calculate dL/dB?
 dZ/dB = I
 dL/dB = (dL/dY)*(dY/dZ)*(dZ/dB)
 //
 Find dL/dX for back propagation:
 dL/dX = (dL/dY)*(dY/dZ)*(dZ/dX)
 //
 How to back propagate to previous layer?
 dL/dY previous layer = dL/dX current layer,
 Apply dL/dW calculation as above to update weight and bias in previous layer,
 Proceed to calculate dL/dX in previous layer for back prop input (dL/dY) to
 next previous layer.
 //
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
 Reference:
 Convolution multi-node input:
 https://towardsdatascience.com/backpropagation-in-a-convolutional-layer-24c8d64d8509?gi=9e0f2eebeef5

 Convolution - back prop:
 https://bishwarup307.github.io/deep%20learning/convbackprop/
 //

 @author ron cook */
public class ConvoNetTrain extends ConvoNetBase implements ConvoNetI {


    private static final Logger LOG = Logger.getLogger(ConvoNetTrain.class.getName());
    //
    //
    private Matrix xIn;
    // seed for random shuffling of input samples
    private static final long SHUFFLE_SEED = 4321;
    //
    //
    private double accuracyPrev;
    private double accuracy;
    private long convoTime;
    //
    private int batchSampleBase;
    private int totalSamples;
    private int batchSize;
    //
    // doNow flag for debug logging
    private boolean doNow;
    //
    private String status;
    // input list of image samples
    private List<NetData> dataList;
    // list of column matrices for actual output
    private List<Matrix> actualOutList;
    //
    // rateModel:  gradient descent rate
    private EtaModel rateModel;
    // lambda: L2 regularization parameter
    private double lambda;
    // mu:  momentum parameter
    private double mu;


    /**
     Convolution network for training which includes back propagation.
     */
    public ConvoNetTrain(NetResult netResult) {
        super(netResult);
        this.netResult = netResult;
    }

    /**
     Convolution network for training which includes back propagation.
     */
    public ConvoNetTrain(NetResult netResult, NetConfig config) {
        super(netResult);
        this.netResult = netResult;
        this.config = config;
        init();
    }

    /**
     Init.
     */
    public void init() {
        try {
            configureNet(config);
            prepAll();
            if(netResult.fitParams != null){
                setFitParams(netResult.fitParams);
            } else {
                initNet();
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    public void setDoNow(boolean doNow) {
        this.doNow = doNow;
    }

    /**
     Initialize filters, weights, biases
     */
    public void initNet() {
        try {
            LOG.info("Initialize network");
            //
            //
            LOG.info("convoPoolLayers size: " + convoPoolLayers.size());
            for (ConvoPoolLayer convoPoolLayer : convoPoolLayers) {
                ConvoLayer convoLayer = convoPoolLayer.convoLayer;
                LOG.info("convoLayer filterSize: " + convoLayer.getFilterSize());
                LOG.info("convoLayer.getFilterList size: " + convoLayer.getFilterList().size());
                LOG.info("convoLayer numFilters: " + convoLayer.getnOut());
                if (convoLayer.getFilterList().isEmpty()) {
                    LOG.info("convoLayer FilterList is empty, call initFilterList");
                    convoLayer.initFilterList(convoLayer.getFilterSize(), convoLayer.getnOut());
                    convoLayer.initBias(convoLayer.getnOut());
                }
            }
            //
            LOG.info("internalLayers size: " + internalLayers.size());
            for (InternalLayer internalLayer : internalLayers) {
                internalLayer.initLayer();
            }
            outputLayer.initLayer();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Prepare training data sample list.
     Scale to  digitize to small values (0.0, 0.1)

     @return the list of training data
     */
    public List<NetData> prepTrainingData() {
        List<NetData> dataList = new ArrayList<>();
        try {
            String dataDir = config.generalConfig.trainingDir;
            LOG.info("dataDir: " + dataDir + ", numOutputNodes: " + numOutputNodes);
            // numOutputNodes = number of class indexes
            totalSamples = config.generalConfig.totalTrainingSamples;
            LOG.info("totalSamples: " + totalSamples);
            setStatus("Load input samples");
            int numClassesToLoad = numOutputNodes;
            List<List<NetData>> groupedData = null;
            if (totalSamples <= numOutputNodes) {
                int numEachClass = totalSamples / numOutputNodes;
                if (numEachClass == 0) {
                    numClassesToLoad = totalSamples;
                    numEachClass = 1;
                }
                // load image data, only for specified classes
                groupedData = ImageDataUtil.loadData(dataDir, numClassesToLoad,
                        numEachClass);

            } else {
                // totalSamples > numOutputNodes
                // number of files to load for each class
                int numEachClass = totalSamples / numOutputNodes;
                // groupedData list contains separate list for each class index
                groupedData = ImageDataUtil.loadData(dataDir, numClassesToLoad, numEachClass);
            }
            LOG.info("groupedData size: " + groupedData.size());
            //
            for (List<NetData> singleClassData : groupedData) {
                // copy class data to combined data list
                dataList.addAll(singleClassData);
            }
            //
            // randomly shuffle the image data order
            Random rand = new Random(SHUFFLE_SEED);
            // shuffle image data randomly
            Collections.shuffle(dataList, rand);
            //
            int len = dataList.size();
            LOG.info("dataList size: " + len);
            // check total samples size
            if (totalSamples > len) {
                totalSamples = len;
            }
            //
            // check first few image classes
            for (int i = 0; i < numClassesToLoad; i++) {
                NetData d = dataList.get(i);
                LOG.info("actual class index: " + d.getActualIndex());
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return dataList;
    }

    /**
     Perform all preparations before network propagation
     */
    public boolean prepAll() {
        boolean isOk = true;
        try {
            LOG.info("prepTrainingData");
            // Prepare training data sample list.
            dataList = prepTrainingData();
            if (dataList == null) {
                isOk = false;
            }
            if (isOk) {
                LOG.info("prepActualMatrix");
                // Prepare list of actual column matrices for measuring network performance.
                actualOutList = prepActualMatrix();
            } else{
                String msg = "dataList is null";
                LOG.log(Level.SEVERE, msg);
                throw new RuntimeException(msg);
            }
            int len = dataList.size();
            batchSize = config.backPropConfig.batchSize;
            // check batch size
            if (batchSize > len) {
                batchSize = len;
            }
            //
            // Prepare back prop parameters
            prepBackProp();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return isOk;
    }

    /**
     Get total number of data samples

     @return total number of data samples
     */
    public int getTotalSamples() {
        return totalSamples;
    }

    /**
     Get sample batch size

     @return sample batch size
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     Train network for all samples
     */
    public void fit() {
        try {
            LOG.fine("runNet");
            //testing forward propagation
            int totalSamples = config.generalConfig.totalTrainingSamples;
            batchSampleBase = 0;
            for (int k = 0; k < totalSamples; k++) {
                // next batch
                fitBatch();
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Train network for one batch of samples.
     */
    public void fitBatch() {
        try {
            //    LOG.info("batchSize: " + batchSize);
            batchSampleCount = 0;
            batchNumCorrect = 0;
            boolean batchCompleted = false;
            if ((sampleCount + batchSize) <= totalSamples) {
                for (int i = 0; i < batchSize; i++) {
                    // next data sample
                    NetData netData = dataList.get(batchSampleBase + i);
               //         LOG.log(Level.FINE, " netData: " + netData);
                    // next sample image
                    Matrix xIn = netData.getInputData();
            //        LOG.log(Level.FINE, " xIn: " + xIn);
                    //
                    // train one image sample forward through all network layers
                    trainAllLayers(xIn, doNow);
                    //
                    // actual class index for current image sample
                    int actualIndedx = netData.getActualIndex();
                    //  LOG.info("actualIndedx: " + actualIndedx);
                    updateEval(actualIndedx);
                    //prepare backprop
                    // matrix of actual output at given index
                    Matrix actualMatrix = actualOutList.get(actualIndedx);
                    outputLayer.setActualY(actualMatrix);
                    sampleCount++;
                    batchSampleCount++;
                    if (batchSampleCount == batchSize) {
                        batchCompleted = true;
                        // last batch
                        accuracy = ((double) batchNumCorrect / batchSampleCount) * 100.0;
                        // save previous accuracy
                        accuracyPrev = accuracy;
                    }
                    // back prop batch
                    backProp(batchCompleted);
                }
                setStatus("Samples completed: " + sampleCount);
                // update batchSampleBase
                batchSampleBase += batchSize;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Train sample.

     @param xIn input sample
     */
    public void trainAllLayers(Matrix xIn, boolean doNow) {
        try {
            //    LOG.info("trainSample");

            List<Matrix> poolOut = new ArrayList<>();
            // add x input to poolOut
            // input to ConvoLayer may be xIn or poolOut
            poolOut.add(xIn);
            //
            List<Matrix> convoOut = null;
            Matrix internalIn = null;
            Matrix internalOut = null;
            Instant startConvo = Instant.now();
            //training forward propagation convo/pool layers
            for (ConvoPoolLayer convoPool : convoPoolLayers) {
                ConvoLayer convoLayer = convoPool.convoLayer;
                convoLayer.setDoNow(doNow);
                // input to ConvoLayer may be xIn or poolOut
                convoOut = convoLayer.trainForward(poolOut);
                //
                PoolLayer poolLayer = convoPool.poolLayer;
                poolOut = poolLayer.trainForward(convoOut);
            }
            // time passes
            Instant endConvo = Instant.now();
            Duration timeElapsed = Duration.between(startConvo, endConvo);
            convoTime += timeElapsed.toSeconds();

            // concatenate poolOut matrix list to a single matrix for the internal layer
            internalIn = MTX.listToSingleCol(poolOut);
            // in case there is no InternalLayer, init internalOut
            internalOut = internalIn;
            //
            for (InternalLayer internalLayer : internalLayers) {
                internalLayer.setDoNow(doNow);
                internalOut = internalLayer.trainForward(internalIn);
                internalIn = internalOut;
            }
     //       internalOut.checkNaN("internalOut");
            //
            outputLayer.setDoNow(doNow);
            Matrix finalOut = outputLayer.trainForward(internalOut);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Prepare back prop parameters
     */
    public void prepBackProp() {
        try {
            // etaSchedule: gradient descent rate schedule
            rateModel = config.backPropConfig.rateModel;
            // lambda: L2 regularization parameter
            lambda = config.backPropConfig.lambda;
            // mu:  momentum parameter
            mu = config.backPropConfig.mu;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Back prop after batch size samples.
     Traverse layers in reverse order.
     */
    public void backProp(boolean batchCompleted) {
        try {
            //
            double eta = nextEta();
            //
            outputLayer.setEta(eta);
            outputLayer.setLambda(lambda);
            outputLayer.setMu(mu);
            // dLdZ : batch loss function
            Matrix dLdZ = outputLayer.lossFn();
            Matrix dLdXOutput = outputLayer.backProp(dLdZ, batchCompleted);
            //
            Matrix dLdXInternal = dLdXOutput;
            //
            if (internalLayers.size() > 0) {
                // backprop traverses layers in reverse order
                ListIterator<InternalLayer> internalIter
                        = internalLayers.listIterator(internalLayers.size());
                while (internalIter.hasPrevious()) {
                    internalLayer = internalIter.previous();
                    internalLayer.setEta(eta);
                    internalLayer.setLambda(lambda);
                    internalLayer.setMu(mu);
                    dLdXInternal = internalLayer.backProp(dLdXInternal, batchCompleted);
                }
            }
            //
            // get
            if (convoPoolLayers.size() > 0) {
                ConvoPoolLayer convoPool = convoPoolLayers.get(convoPoolLayers.size() - 1);
                PoolLayer poolLayer = convoPool.poolLayer;
                // dLdX actually consists of one matrix for each filter
                List<Matrix> dLdXList = MTX.splitMatrix(dLdXInternal, poolLayer.getoutListSize());
                //
                // backprop traverses layers in reverse order
                ListIterator<ConvoPoolLayer> convoPoolIter
                        = convoPoolLayers.listIterator(convoPoolLayers.size());
                while (convoPoolIter.hasPrevious()) {
                    convoPool = convoPoolIter.previous();
                    //
                    poolLayer = convoPool.poolLayer;
                    List<Matrix> dLdXPool = poolLayer.backProp(dLdXList);
                    //
                    ConvoLayer convoLayer = convoPool.convoLayer;
                    convoLayer.setEta(eta);
                    convoLayer.setLambda(lambda);
                    convoLayer.setMu(mu);
                    List<Matrix> dLdXConvo = convoLayer.backProp(dLdXPool, batchCompleted);
                    // reset poolLayer backprop input
                    dLdXList = dLdXConvo;
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    private double nextEta() {
        double eta = rateModel.minRate;
        try {
            String rateFn = rateModel.rateFn;
            if (rateFn.equalsIgnoreCase("Triangle Decay")) {
                double minPeak = 2.0 * rateModel.minRate;
                eta = MathUtil.decayTriangleFn(rateModel.minRate, rateModel.maxRate, minPeak,
                        rateModel.decayPerStep, rateModel.stepCount, sampleCount);
            } else if (rateFn.equalsIgnoreCase("Step Decay")) {
                eta = MathUtil.decayStepFn(rateModel.minRate, rateModel.maxRate,
                        rateModel.decayPerStep, rateModel.stepCount, sampleCount);
            } else {
                String msg = "Invalid rate function: " + rateFn;
                LOG.log(Level.SEVERE, msg);
                throw new RuntimeException(msg);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return eta;
    }

    public long getConvoTime() {
        return convoTime;
    }
}   // end class
