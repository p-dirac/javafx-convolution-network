package datasci.backend.model;

import datasci.frontend.util.EtaModel;

import java.util.TreeMap;

/**
 * General configuration elements required for running the network
 */
public class GeneralConfig {

    //
    public String trainingDir;
    public String testingDir;
    //
    public int totalTrainingSamples;
    public int totalTestingSamples;
    public int batchSize;
    //
    // etaSchedule: gradient descent rate
    public EtaModel rateModel;
    // lambda: L2 regularization parameter
    public double lambda;
    // mu:  momentum parameter
    public double mu;

    //
    public GeneralConfig() {
    }
}
