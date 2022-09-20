package datasci.backend.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Network model fit parameters, including weights and biases for all layers
 * Note: does not include any back prop parameters; does not include NetConfig
 * parameters.
 * See NetConfig for back prop parameters, layer structure, etc.
 */
public class FitParams {

    // convoPoolList size: # of convolution/pool layers
    public List<ConvoPoolFitParams> convoPoolList = new ArrayList<>();
    //
    // internalList size: # of internal layers
    public List<InternalFitParams> internalList = new ArrayList<>();
    //
    public OutputFitParams outputFitParams;


    public FitParams() {
    }
}
