package datasci.backend.model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MomentumNode {

    private static final Logger LOG = Logger.getLogger(MomentumNode.class.getName());

    // momentumNodeList size: # input feature maps (nIn)
    // contents: one momentum component for each input node
    private List<Matrix> momentumNodeList = new ArrayList<>();


    public MomentumNode() {
    }
    public void add(Matrix v){
        momentumNodeList.add(v);
    }
    public Matrix get(int i){
        return momentumNodeList.get(i);
    }

    public List<Matrix> getMomentumNodeList() {
        return momentumNodeList;
    }

    public void setMomentumNodeList(List<Matrix> momentumNodeList) {
        this.momentumNodeList = momentumNodeList;
    }
}  // end class
