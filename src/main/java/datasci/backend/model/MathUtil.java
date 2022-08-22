package datasci.backend.model;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The type MathUtil.
 */
public class MathUtil {

    private static final Logger LOG = Logger.getLogger(MathUtil.class.getName());
    private static final DecimalFormat fmtThree = new DecimalFormat("##0.###");
    /**
     * In array find index of maximum value
     *
     * @param a the array to search
     * @return index of maximum value
     */
    public static int indexOfMax( double[] a )
    {
        int maxIndex = 0;
        for ( int i = 1; i < a.length; i++ )
        {
            if ( a[i] > a[maxIndex] ){
                maxIndex = i;
            }
        }
        // index of the first max found
        return maxIndex;
    }

    /**
     * In array find index of minimum value
     *
     * @param a the array to search
     * @return index of minimum value
     */
    public static int indexOfMin( double[] a )
    {
        int minIndex = 0;
        for ( int i = 1; i < a.length; i++ )
        {
            if ( a[i] < a[minIndex] ){
                minIndex = i;
            }
        }
        // index of the first min found
        return minIndex;
    }

    public static double getMax(double[] a) {
        return Arrays.stream(a).max().getAsDouble();
    }

    /**
     * Convert array of doubles to String, using
     *
     * @param a the array to search
     * @return index of minimum value
     */
    public static String arraytoString( double[] a )
    {
        String result = null;
        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < a.length; i++ )
        {
                String d = fmtThree.format(a[i]);
                sb.append(d).append(", ");
        }
        result = sb.toString();
        return result;
    }

    /**
     * Sigmoid function: y = 1 / [1 + exp(-x)]
     *
     * @param x independent variable
     * @return y dependent variable
     */
    public static double sigmoid( double x )
    {
        return  1.0 / (1.0 + Math.exp(-x));
    }

    /**
     * General logistic [0,0] to [1,1]
     * Logistic function scaled and translated:
     * from [-10, 0] to [10, 1] -> [0,0] to [1,1]
     * At x = 0.5, y = 0.5
     *
     * @param x independent variable
     * @return y dependent variable
     */
    public static double genLogistic( double x )
        {
        // input x : [0, 1]
        // Logistic function with x scaled by 10 and translated by +5
        double xTerm = Math.exp(10*x - 5);
        // output y : [0, 1]
        double y = xTerm / (1 + xTerm);
        return  y;
    }

    /**
     * Update weight matrix.
     *
     * @param dLdW  gradient of loss with respect to weight
     * @param eta   gradient descent rate
     * @param w     weight matrix
     * @param v     velocity matrix
     * @param mu    momentum parameter
     * @param oneMinusLambda one minus L2 regularization parameter
     */
    public static void updateWeightMatrix(Matrix dLdW, double eta, Matrix w, Matrix v,
                                          double mu, double oneMinusLambda){
        try{
            // update delta weight matrix
            // dw(n, m) = - eta * dLdW(n, m)
            Matrix dw = MTX.mulConstant(dLdW, -eta);
            // update velocity matrix, initially zero
            MTX.mulConstInPlace(v, mu);
            // update velocity matrix with gradient
            // v = mu * v - eta * dLdW
            MTX.addInplace(v, dw);
            // L2 regularization to reduce weights on back prop
            // W(n, m) = W(n, m) * (1 - lambda)
            MTX.mulConstInPlace(w, oneMinusLambda);
            // update weight matrix
            // W(n, m) = W(n, m) + dw(m, n)
            MTX.addInplace(w, dw);
            w.checkNaN("output backprop w");
            LOG.fine("after update, w : " + w);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Triangle function
     * ref:  https://www.jeremyjordan.me/nn-learning-rate/
     *
     * @param halfCycle step size count for one half cycle
     * @param count number of iterations 0 to total count
     * @return y dependent variable 0 to 1
     */
    public static double triangleFn( int halfCycle, int count )
    {
        double y = 0;
        // cast to double to avoid int truncation
        double ratio = (double)count/halfCycle;
        double cycle = Math.floor(1 + 0.5*ratio);
        // yflip: 1 to 0 over half cycle
        double yflip = Math.abs(ratio - 2*cycle + 1);
        // output y : 0 to 1 over half cycle
        y = Math.max(0, 1.0 - yflip);
        return  y;
    }
    /**
     * Triangle function with decay
     * ref:  https://www.jeremyjordan.me/nn-learning-rate/
     *
     * @param minY min Y value
     * @param maxY max Y value
     * @param minPeak min of peak Y value
     * @param decay maxY decay per cycle count (every 1 steps)
     * @param stepSize step size count for one cycle
     * @param count number of iterations 0 to total count
     * @return y dependent variable minY to peakY (maxY that decays)
     */
    public static double decayTriangleFn( double minY, double maxY, double minPeak, double decay, int stepSize, int count )
    {
        double y = 0;
        double triFn = triangleFn( stepSize/2, count );
     //   LOG.info("triFn: " + triFn);
        // peakY: minY to maxY, decreases by decay as count increase
        double peakY = decayStepFn( minY, maxY, decay, stepSize, count );
     //   LOG.info("peakY: " + peakY);
        double diffY = peakY - minY;
        // output y : [minY, peakY]
        y = minY + diffY * triFn;
     //   LOG.info("diffY: " + diffY + ", minY: " + minY);
        return  y;
    }
    /**
     * Step function with decay
     * ref:  https://www.jeremyjordan.me/nn-learning-rate/
     *
     * @param minY min Y value
     * @param minY max Y value
     * @param decay maxY decay per stepSize
     * @param stepSize step size count for one cycle
     * @param count number of iterations 0 to total count
     * @return y dependent variable minY to peakY (maxY that decays)
     */
    public static double decayStepFn( double minY, double maxY, double decay, int stepSize, int count )
    {
        double y = 0;
        //  int truncation
        int nSteps = count/stepSize;
        // peakY: minY to maxY, decreases by decay as count increase
        double dY = decay*nSteps;
        double peakY = Math.max(minY, maxY - decay*nSteps);
    //    LOG.info("nSteps: " + nSteps + ", dY: " + dY + ", peakY: " + peakY);
        // y : minY to maxY
        y = peakY;
        return  y;
    }

    /**
     * Sine function with exponential decay
     *
     * ref:  https://www.jeremyjordan.me/nn-learning-rate/
     *
     * @param halfCycle step size count for one half cycle
     * @param count number of iterations 0 to total count
     * @return y dependent variable 0 to 1
     */
    public static double dampedSineFn( double decay, int halfCycle, int count )
    {
        double twopi = 2*Math.PI;
        double maxY = Math.exp(-decay*count);
        double y = 0;
        // cast to double to avoid int truncation
        double theta = (double)count/(2*halfCycle);
        // output y : [0, maxY]
        y = maxY*Math.abs(Math.sin(Math.PI*theta));
        return  y;
    }

}  //end class
