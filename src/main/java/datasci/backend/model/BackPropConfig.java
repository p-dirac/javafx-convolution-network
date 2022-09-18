package datasci.backend.model;

/**
 * Hyperparameter configuration elements required for running the network
 */
public class BackPropConfig {

    //
    // etaSchedule: gradient descent rate
    public EtaModel rateModel;
    // lambda: L2 regularization parameter
    public double lambda;
    // mu:  momentum parameter
    public double mu;
    public int batchSize;
    //
    public BackPropConfig() {
    }
}
