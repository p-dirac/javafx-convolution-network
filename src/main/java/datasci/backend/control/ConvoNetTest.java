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

import datasci.backend.layers.ConvoLayer;
import datasci.backend.layers.ConvoPoolLayer;
import datasci.backend.layers.InternalLayer;
import datasci.backend.layers.PoolLayer;
import datasci.backend.model.ImageDataUtil;
import datasci.backend.model.MTX;
import datasci.backend.model.Matrix;
import datasci.backend.model.NetConfig;
import datasci.backend.model.NetData;
import datasci.backend.model.NetResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation convolution network for image classification on MNIST dataset
 * <p>
 * ref: https://www.analyticsvidhya.com/blog/2020/02/mathematics-behind-convolutional-neural-network/
 * <p>
 * Notation for each layer:
 * X = input matrix (e.g. matrix of image pixel values)
 * W = weight matrix
 * B = bias matrix
 * Z = W*X + B
 * S = activation function (e.g. sigmoid, RELU, softmax)
 * Y = S(Z)
 * Y = output matrix
 * L = loss function (e.g. sum square error)
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
 * number of pool output nodes: nfp * nfp * numFilters
 * <p>
 * Example:
 * input image = (28, 28)
 * filter matrix = (5,5)
 * nf = 28 - 5 + 1 = 24, convolution output matrix
 * convolution output matrix = (24 , 24)
 * pool matrix = (2,2)
 * nfp = nf/2 = 24/2 = 12, pool output matrix size
 * pool output matrix = (12 , 12)
 * <p>
 *
 * @author cook
 */
public class ConvoNetTest extends ConvoNetBase implements ConvoNetI {


    private static final Logger LOG = Logger.getLogger(ConvoNetTest.class.getName());
    //
    private Matrix xIn;
    // seed for random shuffling of input samples
    private static final long SHUFFLE_SEED = 4321;
    //
    //
    private int batchSize;
    private int batchSampleBase;
    private int totalSamples;
    //
    private String status;
    // input list of image samples
    private List<NetData> dataList;
    // list of column matrices for actual output
    private List<Matrix> actualIndexList;
    //
    // etaSchedule: key = sample count, value = gradient descent rate
    private TreeMap<Integer, Double> etaSchedule;


    /**
     * Convolution network for testing (no back propagation)
     */
    public ConvoNetTest(NetResult netResult) {
        super(netResult);
        this.netResult = netResult;
    }

    /**
     * Convolution network for testing (no back propagation).
     */
    public ConvoNetTest(NetResult netResult, NetConfig config) {
        super(netResult);
        this.netResult = netResult;
        this.config = config;
        init();
    }

    /**
     * Initialize the network: set layer configurations, load sample data
     */
    public void init(){
        try{
            configureNet(config);
            prepAll();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }


    /**
     * Perform all preparations before network propagation
     */
    public boolean prepAll() {
        boolean isOk = true;
        try {
            LOG.info("prepTestingData");
            // Prepare test data sample list.
            dataList = prepTestData();
            if (dataList == null) {
                isOk = false;
            }
            if (isOk) {
                LOG.info("prepActualMatrix");
                // Prepare list of actual column matrices for measuring network performance.
                actualIndexList = prepActualMatrix();
            }
            int len = dataList.size();
            batchSize = config.generalConfig.batchSize;
            // check batch size
            if (batchSize > len) {
                batchSize = len;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return isOk;
    }

    /**
     * Get total number of data samples
     *
     * @return total number of data samples
     */
    public int getTotalSamples(){
        return totalSamples;
    }
    /**
     * Get sample batch size
     *
     * @return sample batch size
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * Train sample.
     *
     * @param xIn input sample
     */
    public void testAllLayers(Matrix xIn) {
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
            //training forward propagation
            for (ConvoPoolLayer convoPool : convoPoolLayers) {
                ConvoLayer convoLayer = convoPool.convoLayer;
                // input to ConvoLayer may be xIn or poolOut
                convoOut = convoLayer.testForward(poolOut);
                //
                PoolLayer poolLayer = convoPool.poolLayer;
                poolOut = poolLayer.testForward(convoOut);
            }
            // concatenate poolOut matrix list to a single matrix for the internal layer
            internalIn = MTX.listToSingleCol(poolOut);
            LOG.fine("internalIn : " + internalIn);
            //
            for (InternalLayer internal : internalLayers) {
                internalOut = internal.testForward(internalIn);
                internalIn = internalOut;
            }
            internalOut.checkNaN("internalOut");
            //
            Matrix finalOut = outputLayer.testForward(internalOut);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }


    /**
     * Prepare test data list.
     *
     * @return the list of test data
     */
    public List<NetData> prepTestData() {
        List<NetData> dataList = new ArrayList<>();
        try {
            LOG.info("prepTestData");
            String dataDir = config.generalConfig.testingDir;

            LOG.info("dataDir: " + dataDir);
            // numOutputNodes = number of class indexes
            totalSamples = config.generalConfig.totalTestingSamples;
            // number of files to load for each class
            int numEachclass = totalSamples / numOutputNodes;
            // load image data, only for specified classes
            // groupedData list contains separate list for each class index
            List<List<NetData>> groupedData = ImageDataUtil.loadData(dataDir, numOutputNodes, numEachclass);
            //
            for (List<NetData> singleData : groupedData) {
                // copy class data to combined data list
                dataList.addAll(singleData);
            }
            Random rand = new Random(SHUFFLE_SEED);
            //
            // shuffle image data randomly
            Collections.shuffle(dataList, rand);
            //
            int len = dataList.size();
            LOG.info("dataList size: " + len);
            // check total samples size
            if (totalSamples > len) {
                totalSamples = len;
            }
            // check first few image classes
            for(int i = 0; i < 10; i++) {
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
     * Test network for all samples
     */
    public void fit() {
        try {
            LOG.fine("runNet");
            //testing forward propagation
            int totalSamples = config.generalConfig.totalTestingSamples;
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
     * Test network for one batch of samples.
     */
    public void fitBatch() {
        try {
            LOG.fine("runNetBatch");
            //testing forward propagation
            batchSampleCount = 0;
            batchNumCorrect = 0;
            for (int i = 0; i < batchSize; i++) {
                // next data sample
                NetData netData = dataList.get(batchSampleBase + i);
                LOG.fine("netData: " + netData);
                // next sample image
                Matrix xIn = netData.getInputData();
                //
                // test one image sample forward through all network layers
                testAllLayers(xIn);
                //
                // actual class index for current image sample
                int actualIndedx = netData.getActualIndex();
                //  LOG.info("actualIndedx: " + actualIndedx);
                updateEval(actualIndedx);
                sampleCount++;
                batchSampleCount++;
            }
            setStatus("Samples completed: " + sampleCount);
            // update batchSampleBase
            batchSampleBase += batchSize;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }


}   // end class
