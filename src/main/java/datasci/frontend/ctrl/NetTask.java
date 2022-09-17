package datasci.frontend.ctrl;
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

import datasci.backend.control.ConvoNetI;
import datasci.backend.control.ConvoNetTest;
import datasci.backend.control.ConvoNetTrain;
import datasci.backend.model.EvaluationR;
import datasci.backend.model.NetConfig;
import datasci.backend.model.NetResult;
import datasci.backend.model.FitParams;
import javafx.concurrent.Task;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Task to run convolution network
 */
public class NetTask extends Task<TaskResult> {

  //  private static final Logger LOG = Logger.getLogger(NetTask.class.getName());
    private static Logger LOG = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static final DecimalFormat SIZE_FMT = new DecimalFormat("##0.##");

    private int totalSamples;
    private int batchSize;

    private int subsetSize;
    public int subsetCompleted;
    public int subsetCorrect;
    //
    private final NetConfig config;
    //
    private String netOption;
    private static final String TRAIN_NET = "Train Net";
    private static final String TEST_NET = "Test Net";
    //
    private ConvoNetI net;


    /**
     * Invoke new task for convolutional network.
     *
     * @param config network configuration
     */
    public NetTask(NetConfig config, String netOption) {
        this.config = config;
        this.netOption = netOption;
    }

    @Override
    protected void scheduled() {
        super.scheduled();
        updateMessage("Task is scheduled");
    }

    @Override
    protected void running() {
        super.running();
        updateMessage("Task is running");
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        //   updateMessage("Task completed successfully");
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        //  updateMessage("Task cancelled");
    }

    @Override
    protected void failed() {
        super.failed();
        //   updateMessage("Task failed");
    }

    public void setSubsetSize(int subsetSize) {
        this.subsetSize = subsetSize;
    }

    /**
     * Task 'call' method is called when task thread 'start' method is called.
     * Execute the network, update progress, and return results
     *
     * @return result of network predictions
     * @throws Exception
     */
    @Override
    protected TaskResult call() throws Exception {
        TaskResult result = new TaskResult();
        try {
            String status = null;
            result.netResult = new NetResult();
            FitParamsCache fitParamsCache = FitParamsCache.getInstance();
            FitParams fitParams = fitParamsCache.getFitParams();
            if(fitParams != null){
                result.netResult.fitParams = fitParams;
            }
            if (TRAIN_NET.equalsIgnoreCase(netOption)) {
                // training network
                net = new ConvoNetTrain(result.netResult, config);
            } else if (TEST_NET.equalsIgnoreCase(netOption)) {
                // testing network
                net = new ConvoNetTest(result.netResult, config);
            }
            //
            totalSamples = net.getTotalSamples();
            batchSize = net.getBatchSize();
            //
            LOG.info("totalSamples: " + totalSamples + ", subsetSize: " + subsetSize + ", batchSize: " + batchSize);
            // loop over multiple subsetSize up to totalTrainingSamples
            for (int samples = 0; samples < totalSamples; samples += subsetSize) {
                LOG.info("samples: " + samples);
                // reset sub sample counts
                subsetCompleted = 0;
                subsetCorrect = 0;
                Instant start = Instant.now();
                //
            //    net.setDoNow(true);
                // loop over multiple batchSize up to subsetSize
                for (int subSamples = 0; subSamples < subsetSize; subSamples += batchSize) {
                    LOG.fine("subSamples: " + subSamples);
                    //    LOG.info("isCancelled: " + isCancelled());
                    if (isCancelled()) {
                        updateMessage("Task cancelled, isCancelled: " + isCancelled());
                        LOG.info("task cancelled after " + samples + " samples");
                        break;
                    }

                    //
                    // continue training the network for batchSize samples
                    net.fitBatch();
                    //
                    net.setDoNow(false);

                    //
                    // evaluate after each batch
                    EvaluationR eval = net.evaluate();
                    //
                    int sampleCompleted = eval.sampleCount();
                    int sampleCorrect = eval.numCorrect();
                    // update result
                    result.netResult.samplesCompleted = sampleCompleted;
                    result.netResult.samplesCorrect = sampleCorrect;
                    result.netResult.batchSamplesCompleted = eval.batchSampleCount();
                    result.netResult.batchSamplesCorrect = eval.batchNumCorrect();
                    //     LOG.info("batch Completed: " + eval.batchSampleCount() + ", batch Correct: " + eval.batchNumCorrect());
                    // update sub sample counts
                    subsetCompleted += eval.batchSampleCount();
                    subsetCorrect += eval.batchNumCorrect();
                }
                //
                net.setDoNow(false);
                //
                // after each subsetSize, update results
                //
                status = net.getStatus();
                //
                updateMessage(status);
                LOG.info("status: " + status);
                //
                // evaluate after each subset
                EvaluationR eval = net.evaluate();
                int sampleCompleted = eval.sampleCount();
                int sampleCorrect = eval.numCorrect();
                // update result
                result.netResult.samplesCompleted = sampleCompleted;
                result.netResult.samplesCorrect = sampleCorrect;
                //
                result.subsetCompleted = subsetCompleted;
                result.subsetCorrect = subsetCorrect;
                LOG.info("subsetCompleted: " + subsetCompleted + ", subsetCorrect: " + subsetCorrect);
                //
                // update task value before progress
                updateValue(result);
                // subset percent correct
                double percentSubset = ((double) subsetCorrect / subsetCompleted) * 100.0;
                String subsetAccuracy = SIZE_FMT.format(percentSubset);
                LOG.info("subsetAccuracy: " + subsetAccuracy);

                // cumulative percent correct
                double percentCorrect = ((double) sampleCorrect / sampleCompleted) * 100.0;
                String accuracy = SIZE_FMT.format(percentCorrect);
                LOG.info("accuracy: " + accuracy);
                //
                // Task has properties such as: message, progress
                updateMessage("Overall Accuracy: " + accuracy + " %,    Subset Accuracy: " + subsetAccuracy + " %");
                LOG.info("sampleCorrect: " + sampleCorrect);
                LOG.info("sampleCompleted: " + sampleCompleted);
                //
                // update progress last, since it will trigger listener
                updateProgress(sampleCompleted, totalSamples);
                // time passes
                Instant end = Instant.now();
                Duration timeElapsed = Duration.between(start, end);
                LOG.info("subset timeElapsed (sec): " + timeElapsed.toSeconds());
            }
            //
            if (!isCancelled()) {
                updateMessage("Training completed");
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            updateMessage("Task exception: " + ex.getMessage());
            this.failed();
            throw new RuntimeException(ex);
        }
        return result;
    }

    public FitParams prepFitParams() {
        NetResult res = net.getNetResult();
        try {
            FitParams soln = res.fitParams;
            if (soln == null) {
                res.fitParams = net.createFitParams();
            }
            // also save FitParams in cache for other panels to use
            FitParamsCache.getInstance().setFitParams(soln);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return res.fitParams;
    }


    public ConvoNetI getNet() {
        return net;
    }
}