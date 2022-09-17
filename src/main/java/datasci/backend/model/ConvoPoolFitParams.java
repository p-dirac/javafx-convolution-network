package datasci.backend.model;

import java.util.List;

public class ConvoPoolFitParams {
    // fit parameters for one convolutional layer
    public String layerID;
    // filterList:
    //    outer list size: # output feature maps, nOut
    //    ConvoNode size: # input feature maps, nIn
    // total number of filters: nOut * nIn
    public List<ConvoNode> filterList;
    // b matrix: 1 col, nOut rows
    public Matrix bias;

    public ConvoPoolFitParams() {
    }
}
