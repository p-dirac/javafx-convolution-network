package datasci.frontend.ctrl;

import datasci.backend.model.FitParams;

/**
 * Singleton class for storing shared data
 */
public class FitParamsCache {

    //
    /**
     * Network training FitParams, including weights and biases for all layers
     * Note: does not include any back prop parameters
     */
    private FitParams FitParams;

    private final static FitParamsCache INSTANCE = new FitParamsCache();

    private FitParamsCache() {
    }

    public static FitParamsCache getInstance() {
        return INSTANCE;
    }

    public FitParams getFitParams() {
        return FitParams;
    }

    public void setFitParams(FitParams fitParams) {
        this.FitParams = fitParams;
    }
}
