package datasci.backend.layers;

import datasci.backend.model.MTX;
import datasci.backend.model.Matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Network Pooling Layer.
 * <p>
 * reference:
 * https://www.analyticsvidhya.com/blog/2020/02/mathematics-behind-convolutional-neural-network/
 * for backprop pool layer: https://victorzhou.com/blog/intro-to-cnns-part-2/
 * <p>
 * Notes:
 * (Rows, columns) of input image = (n, n), n = image size
 * (Rows, columns) of filter matrix = (f,f), f = filter size
 * let nf = n - f + 1
 * (Rows, columns) of convolution output matrix = ( nf , nf )
 * (Rows, columns) of pool matrix = (p,p)
 * let nfp = (n-f+1) / p = nf / p
 * (Rows, columns) of pool output matrix = ( nfp , nfp )
 * <p>
 * Example:
 * input image = (28, 28)
 * filter matrix = (5,5)
 * nf = 28 - 5 + 1 = 24, convolution output matrix
 * convolution output matrix = (24 , 24)
 * pool matrix = (2,2)
 * nfp = nf/2 = 24/2 = 12, pool output matrix size
 * pool output matrix = (12 , 12)
 */
public class PoolLayer {

    private static final Logger LOG = Logger.getLogger(PoolLayer.class.getName());
    //
    private final LayerE layerType = LayerE.POOL;
    //
    private int poolSize;
    // trainForward input matrix list from convolution layer
    private List<Matrix> inList;
    // trainForward output matrix list to next layer
    private List<Matrix> outList;
    // matrix list of indexes where max pool occurred in each input matrix
    private List<Matrix> indexList;
    // ID for debug purposes
    private String layerID;

    /**
     * Instantiates a new Pool layer.
     */
    public PoolLayer() {
    }

    /**
     * Create a new Pool layer.
     *
     * @param poolSize the pool rows and cols size
     */
    public PoolLayer(int poolSize) {
        this.poolSize = poolSize;
    }


    public String getLayerID() {
        return layerID;
    }

    public void setLayerID(String layerID) {
        this.layerID = layerID;
    }

    public LayerE getLayerType() {
        return layerType;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    /**
     * Perform forward propagation for this network layer
     *
     * @param inList input to this layer; treat matrix x as rectangular, not a one column matrix
     * @return list of matrix output yOut from this layer; treat matrix yOut as a rectangular matrix
     */
    public List<Matrix> trainForward(List<Matrix> inList) {
        this.inList = inList;
        outList = new ArrayList<>();
        try {
            indexList = new ArrayList<>();
            // create rectangular pool matrix yOut from input matrix x
            // matrix yOut: number of rows = x #rows / poolRows
            // matrix yOut: number of cols = x #cols / poolCols
            //
            // find pool matrix from each input matrix
            int inSize = inList.size();
       //     LOG.fine("inList size: " + inSize);
            for (int k = 0; k < inSize; k++) {
                // pool input matrix x(nf, nf), where nf = n - f + 1
                Matrix x = inList.get(k);
            //    x.checkNaN("PoolLayer x before maxPool");
                // stride = poolSize
                // yOut: pool output matrix ( nfp , nfp ), where nfp = (n-f+1) / p
                Matrix y = MTX.maxPool(x, poolSize, poolSize);
            //    y.checkNaN("pool y after maxPool");
                // normalize output to prevent infinity
       //         MTX.normalizeInPlace(y);

                // number of matrix y in outList = inList size
                outList.add(y);
          //     LOG.fine("pool input x : " + x);
          //     LOG.fine("pool output y : " + y);
                //
                // save cell where max pool occurred
                // yOut: pool output matrix ( nfp , nfp ), where nfp = (n-f+1) / p
                Matrix poolIndex = MTX.poolIndex(x, poolSize, poolSize);
                indexList.add(poolIndex);
           //     LOG.fine("trainForward, poolIndex : " + poolIndex);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
     //   LOG.fine("outList size: " + outList.size());
        return outList;
    }

    /**
     * Testing phase has no back propagation
     * Perform forward propagation for this network layer
     *
     * @param inList input to this layer; treat matrix x as rectangular, not a one column matrix
     * @return list of matrix output yOut from this layer; treat matrix yOut as a rectangular matrix
     */
    public List<Matrix> testForward(List<Matrix> inList) {
        this.inList = inList;
        List<Matrix> outList = new ArrayList<>();
        // create square pool matrix yOut from input matrix x
        // matrix yOut: number of rows = x #rows / poolRows
        // matrix yOut: number of cols = x #cols / poolCols
        //
        // find pool matrix from each input matrix
        int n = inList.size();
        for (int k = 0; k < n; k++) {
            Matrix x = inList.get(k);
            Matrix y = MTX.maxPool(x, poolSize, poolSize);
            outList.add(y);
        }
        return outList;
    }

    /**
     * Perform backward propagation for this network layer
     *
     * @param dLdYList derivative of loss function with respect to layer output y
     * @return derivative of layer output loss with respect to layer input dL/dX
     */
    public List<Matrix> backProp(List<Matrix> dLdYList) {
        // dL/dX = dLdY
        // There will be a dLdX(nf, nf) matrix for each input matrix
        List<Matrix> dLdXList = new ArrayList<>();
        try {
            // pool input matrix, firstIn(nf, nf), where nf = n - f + 1, n = image size, f = filter size
            Matrix firstIn = inList.get(0);
            int nf = firstIn.rows;
            LOG.fine("firstIn : " + firstIn);
            //
            int numFilters = outList.size();
            // dLdY actually consists of multiple matrix, one for each filter
            LOG.fine("dLdYList size: " + dLdYList.size() + ", numFilters: " + numFilters);
            // backNum: number of matrix in index list
            int indexListSize = indexList.size();
            //
            for (int b = 0; b < indexListSize; b++) {
                // There will be a dLdX(nf, nf) matrix for each input matrix
                // dLdX(nf, nf) same size as pool input matrix
                Matrix dLdX = new Matrix(nf, nf);
                // dLdYOne(nfp*nfp, 1) same size as pool output matrix formatted as one column
                Matrix dLdYOne = dLdYList.get(b);
                // poolIndex(nfp, nfp) same size as pool output matrix
                Matrix poolIndex = indexList.get(b);
                LOG.fine("dLdX : " + dLdX);
                LOG.fine("dLdYOne : " + dLdYOne);
                LOG.fine("poolIndex : " + poolIndex);
                // size of one pool index matrix
                int poolIndexSize = poolIndex.size;
                LOG.fine("indexListSize: " + indexListSize + ", poolIndexSize: " + poolIndexSize);
                //
                for (int bk = 0; bk < poolIndexSize; bk++) {
                    // dLdX cell index for back prop
                    int cellK = (int) poolIndex.a[bk];
                    LOG.fine("bk: " + bk + ", cellK: " + cellK);
                    // get dLdY value to back propagate
                    double cellVal = dLdYOne.a[bk];
                    //    LOG.fine("backProp, bk: " + bk + ", cellK: " + cellK + ", cellVal: " + cellVal);
                    // set dLdX cell with dLdY value
                    MTX.setCell(dLdX, cellK, cellVal);
                }
                //
          //     MTX.normalizeInPlace(dLdX);
                //
                dLdXList.add(dLdX);
            }
            LOG.fine("dLdXList size : " + dLdXList.size());
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }

        return dLdXList;
    }

    public int getoutListSize() {
        return outList.size();
    }
} // end class