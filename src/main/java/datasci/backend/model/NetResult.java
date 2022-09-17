package datasci.backend.model;

import java.util.logging.Logger;

/**
 * NetResult will hold the overall summary of network performance, and also the network training FitParams
 */
public class NetResult {
    private static final Logger LOG = Logger.getLogger(NetResult.class.getName());

    // confusion matrix: column is predicted index, row is actual index
    public Matrix summaryResults;
    //
    public int samplesCompleted;
    public int samplesCorrect;
    //
    public int batchSamplesCompleted;
    public int batchSamplesCorrect;
    //
    public long convoTime;
    //

    /**
     * Network training FitParams, including weights and biases for all layers
     * Note: does not include any back prop parameters
     */
    public FitParams fitParams;

    /**
     * Constructor.
     */
    public NetResult() {
    }

    /**
     * Update summary values
     * @param actualIndex output actual value
     * @param predictedIndex output predicted value
     */
    public void updateSummary(int actualIndex, int predictedIndex) {
        // confusion matrix: row is actual index, column is predicted index
        int rowI = actualIndex;
        int colJ = predictedIndex;
        // increment value by 1
        MTX.updateCell(summaryResults, rowI, colJ,1.0);
    }

    @Override
    public String toString() {
        return "NetResult{" +
                "summaryResults=" + summaryResults +
                ", samplesCompleted=" + samplesCompleted +
                ", samplesCorrect=" + samplesCorrect +
                ", batchSamplesCompleted=" + batchSamplesCompleted +
                ", batchSamplesCorrect=" + batchSamplesCorrect +
                '}';
    }
}  //end class
