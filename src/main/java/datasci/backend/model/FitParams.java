package datasci.backend.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Network model fit parameters, including weights and biases for all layers
 * Note: does not include any back prop parameters; does not include NetConfig
 * constant parameters
 */
public class FitParams {

    //
    public List<ConvoPoolFitParams> convoPoolList = new ArrayList<>();
    //
    public List<InternalFitParams> internalList = new ArrayList<>();
    //
    public OutputFitParams outputFitParams;


    public FitParams() {
    }
}
