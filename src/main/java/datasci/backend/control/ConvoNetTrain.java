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
import datasci.frontend.util.EtaModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 Implementation of convolution network for image classification on MNIST dataset

 ref: https://www.analyticsvidhya.com/blog/2020/02/mathematics-behind-convolutional-neural-network/

 ref: https://towardsdatascience.com/deriving-the-backpropagation-equations-from-scratch-part-2-693d4162e779

 eta triangle function:
 ref: https://www.jeremyjordan.me/nn-learning-rate/

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

 // ------------
 Matrix sizing:
 Input Layer:
 (Rows, columns) of input image = (n, n)
 n = image size
 Convolution Layer:
 filter matrix = (f,f),
 f = filter size
 let nf = n - f + 1
 convolution output matrix = ( nf , nf )
 Pool Layer:
 pool matrix = (p,p)
 let nfp = (n-f+1) / p = nf / p
 pool output matrix = ( nfp , nfp )
 number of pool output nodes:
 pOut = nfp * nfp * numFilters
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
 1st Convolution Layer:
 filter matrix = (5,5)
 nf = 28 - 5 + 1 = 24,
 convolution output matrix = (24 , 24)
 numFilters = 20
 1st Pool Layer:
 pool matrix = (2,2)
 nfp = nf/2 = 24/2 = 12,
 pool output matrix = (12 , 12)
 pool output nodes, pOut = 12 * 12 * 20 = 2880
 2nd Convolution/Pool Layer:
 filter matrix = (5,5)
 nf = 12 - 5 + 1 = 8,
 convolution output matrix = (8 , 8)
 numFilters = 50
 2nd Pool Layer:
 pool matrix = (2,2)
 nfp = nf/2 = 8/2 = 4,
 pool output matrix = (4 , 4)
 pool output nodes, pOut = 4 * 4 * 50 = 800
 Internal Layer:
 internal layer input matrix, (pOut,1) = (800, 1)
 internal layer output matrix, (iOut, 1) = (500, 1)
 Output Layer:
 output layer input matrix, (iOut, 1) = (500, 1)
 output layer output matrix, (oOut, 1) = (10, 1)

 // ------------
 Objective: iterate over multiple inputs, update weight and bias to minimize the loss
 predicted y = softmax(z)
 column vector loss, dLdZ[k] = (pred y[k]) -  (actual y[k])
 where k is the class index (0 to 9 for digit classification)

 dL/dW = change in loss due to change in weight
 dL/dB = change in loss due to change in bias
 eta = gradient descent rate

 Weight correction at each iteration from i to i+1:
 W(i + 1) = W(i) - eta*(dL/dW)

 Bias correction at each iteration from i to i+1:
 B(i + 1) = B(i) - eta*(dL/dB)

 How to calculate dL/dW?
 dL/dW = (dL/dY)*(dY/dZ)*(dZ/dW)
 How to calculate dL/dB?
 dL/dB = (dL/dY)*(dY/dZ)*(dZ/dB)

 Find dL/dX for back propagation:
 dL/dX = (dL/dY)*(dY/dZ)*(dZ/dX)

 How to back propagate to previous layer?
 dL/dY previous layer = dL/dX current layer,
 then apply dL/dW calculation as above to update weight and bias in previous layer,
 then calculate dL/dX in previous layer for back prop input (dL/dY) to next previous layer

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
    //
    private int batchSampleBase;
    private int totalSamples;
    private int batchSize;
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
            initNet();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
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
                LOG.info("convoLayer numFilters: " + convoLayer.getNumFilters());
                if (convoLayer.getFilterList().isEmpty()) {
                    LOG.info("convoLayer FilterList is empty, call initFilterList");
                    convoLayer.initFilterList(convoLayer.getFilterSize(), convoLayer.getNumFilters());
                } else {
                    convoLayer.setFilterList(convoLayer.getFilterList());
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
            }
            int len = dataList.size();
            batchSize = config.generalConfig.batchSize;
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
            if ((sampleCount + batchSize) <= totalSamples) {
                for (int i = 0; i < batchSize; i++) {
                    // next data sample
                    NetData netData = dataList.get(batchSampleBase + i);
                    LOG.fine("netData: " + netData);
                    // next sample image
                    Matrix xIn = netData.getInputData();
                    //
                    // train one image sample forward through all network layers
                    trainAllLayers(xIn);
                    //
                    // actual class index for current image sample
                    int actualIndedx = netData.getActualIndex();
                    //  LOG.info("actualIndedx: " + actualIndedx);
                    updateEval(actualIndedx);
                    //prepare backprop
                    // matrix of actual output at given index
                    Matrix actualMatrix = actualOutList.get(actualIndedx);
                    outputLayer.setActualY(actualMatrix);
                    LOG.fine("actualMatrix: " + actualMatrix);
                    outputLayer.updateBatchLoss();
                    sampleCount++;
                    batchSampleCount++;
                }
                // after each batch do back propagation
                backProp();
                setStatus("Samples completed: " + sampleCount);
                //    LOG.fine("sampleCount completed: " + sampleCount);
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
    public void trainAllLayers(Matrix xIn) {
        try {
            //    LOG.info("trainSample");

            List<Matrix> poolOut = new ArrayList<>();
            // add x input to poolOut
            // input to ConvoLayer may be xIn or poolOut
            poolOut.add(xIn);
            LOG.fine("poolOut : " + poolOut);
            //
            List<Matrix> convoOut = null;
            Matrix internalIn = null;
            Matrix internalOut = null;
            //training forward propagation
            for (ConvoPoolLayer convoPool : convoPoolLayers) {
                ConvoLayer convoLayer = convoPool.convoLayer;
                // input to ConvoLayer may be xIn or poolOut
                convoOut = convoLayer.trainForward(poolOut);
                LOG.fine("convoOut : " + convoOut);
                //
                PoolLayer poolLayer = convoPool.poolLayer;
                poolOut = poolLayer.trainForward(convoOut);
                LOG.fine("poolOut : " + poolOut);
            }
            LOG.fine("convoOut size : " + convoOut.size() + ", convoOut 0: " + convoOut.get(0));
            LOG.fine("poolOut size : " + poolOut.size() + ", poolOut 0: " + poolOut.get(0));
            // concatenate poolOut matrix list to a single matrix for the internal layer
            internalIn = MTX.listToSingleCol(poolOut);
            LOG.fine("internalIn : " + internalIn);
            //
            for (InternalLayer internalLayer : internalLayers) {
                internalOut = internalLayer.trainForward(internalIn);
                LOG.fine("internalOut : " + internalOut);
                internalIn = internalOut;
            }
            internalOut.checkNaN("internalOut");
            //
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
            rateModel = config.generalConfig.rateModel;
            // lambda: L2 regularization parameter
            lambda = config.generalConfig.lambda;
            // mu:  momentum parameter
            mu = config.generalConfig.mu;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Back prop after batch size samples.
     Traverse layers in reverse order.
     */
    public void backProp() {
        try {
            // last batch
            accuracy = ((double) batchNumCorrect / batchSampleCount) * 100.0;
            LOG.fine("accuracy: " + accuracy);
            //
            double eta = nextEta();
            //
            outputLayer.setEta(eta);
            outputLayer.setLambda(lambda);
            outputLayer.setMu(mu);
            // dLdZ : batch loss function
            Matrix dLdZ = outputLayer.batchLossFn();
            Matrix dLdXOutput = outputLayer.backProp(dLdZ);
            LOG.fine("loss dLdZ: " + dLdZ);
            LOG.fine("out backprop, dLdXOutput: " + dLdXOutput);
            //
            Matrix dLdXInternal = dLdXOutput;
            //
            if (internalLayers.size() > 0) {
                // backprop traverses layers in reverse order
                ListIterator<InternalLayer> internalIter
                        = internalLayers.listIterator(internalLayers.size());
                while (internalIter.hasPrevious()) {
                    internalLayer = internalIter.previous();
                    LOG.fine("internal layer");
                    internalLayer.setEta(eta);
                    internalLayer.setLambda(lambda);
                    internalLayer.setMu(mu);
                    dLdXInternal = internalLayer.backProp(dLdXInternal);
                }
            }
            LOG.fine("dLdXInternal: " + dLdXInternal);
            //
            // get
            if (convoPoolLayers.size() > 0) {
                ConvoPoolLayer convoPool = convoPoolLayers.get(convoPoolLayers.size() - 1);
                PoolLayer poolLayer = convoPool.poolLayer;
                // dLdX actually consists of one matrix for each filter
                LOG.fine("poolLayer outListSize: " + poolLayer.getoutListSize());
                List<Matrix> dLdXList = MTX.splitMatrix(dLdXInternal, poolLayer.getoutListSize());
                LOG.fine("dLdXList size: " + dLdXList.size());
                //
                // backprop traverses layers in reverse order
                ListIterator<ConvoPoolLayer> convoPoolIter
                        = convoPoolLayers.listIterator(convoPoolLayers.size());
                while (convoPoolIter.hasPrevious()) {
                    convoPool = convoPoolIter.previous();
                    //
                    LOG.fine("pool layer");
                    poolLayer = convoPool.poolLayer;
                    LOG.fine("inside convo pool loop dLdXList size: " + dLdXList.size());
                    List<Matrix> dLdXPool = poolLayer.backProp(dLdXList);
                    LOG.fine("inside convo pool loop dLdXPool size: " + dLdXPool.size());
                    //
                    LOG.fine("convolution layer");
                    ConvoLayer convoLayer = convoPool.convoLayer;
                    convoLayer.setEta(eta);
                    convoLayer.setLambda(lambda);
                    convoLayer.setMu(mu);
                    List<Matrix> dLdXConvo = convoLayer.backProp(dLdXPool);
                    LOG.fine("inside convo pool loop dLdXConvo size: " + dLdXConvo.size());
                    // reset poolLayer backprop input
                    dLdXList = dLdXConvo;
                }
            }
            // save previous accuracy
            accuracyPrev = accuracy;
            LOG.fine("accuracyPrev: " + accuracyPrev);
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
}   // end class
