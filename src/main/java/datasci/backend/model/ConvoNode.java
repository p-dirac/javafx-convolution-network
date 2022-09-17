package datasci.backend.model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 There is one ConvoNode for each convolution output node
 */
public class ConvoNode {
    private static final Logger LOG = Logger.getLogger(ConvoNode.class.getName());

    // filterNodeList size: # input feature maps (nIn)
    // contents: one filter matrix w for each input node
    private List<Matrix> filterNodeList = new ArrayList<>();


    public ConvoNode() {
    }

    public int size() {
        return filterNodeList.size();
    }

    public void add(Matrix w) {
        filterNodeList.add(w);
    }

    public Matrix get(int i) {
        return filterNodeList.get(i);
    }

    public List<Matrix> getFilterNodeList() {
        return filterNodeList;
    }

    public void setFilterNodeList(List<Matrix> filterNodeList) {
        this.filterNodeList = filterNodeList;
    }

    @Override
    public String toString() {

        int len = 0;
        Matrix first = null;
        try {
            len = size();
            first = get(0);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return "{" +
                "size=" + len +
                ", first=" + first +
                '}';
    }
}  // end class
