package datasci.frontend.config;

import datasci.backend.model.Matrix;
import datasci.backend.model.MTX;
import javafx.scene.control.TextField;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The type FilterPane.
 */
public class FilterPane extends GridOne {

    private static final Logger LOG = Logger.getLogger(FilterPane.class.getName());
    private static DecimalFormat fmtTwo = new DecimalFormat("#0.00");
    private static final int FIELD_SIZE = 4;
    // to reproduce results, use same seed for filter layer weights
    private static long FILTER_SEED = 432173271;
    private static long DELTA_SEED = 765432137;
    /**
     * Create new filter grid
     */
    public FilterPane(int rows, int cols) {
        super(rows, cols);
        initCellGaps(2, 2);
        this.setStyle("-fx-border-style: solid; -fx-border-width: 1; -fx-border-color: green;");
    }

    /**
     * Init filter with empty values.
     */
    public void initManual() {
        try {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    // allow user to edit filter values
                    TextField t = new TextField("");
                    t.setStyle("-fx-border-style: solid; -fx-border-width: 1; -fx-border-color: gray;");
                    t.setPrefColumnCount(FIELD_SIZE);
                    // add text to grid
                    setCell(i, j, t);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Init filter matrix with gaussian random values
     */
    public void initRandom() {
        try {
            // to reproduce results, use different seed for each set of filters
            FILTER_SEED += DELTA_SEED;
            Random filterRand = new Random(FILTER_SEED);
            // standard deviation for initial weight
            double stdDev = Math.sqrt(2.0 / (rows * cols));
            //
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    // initialize each weight to random gaussian with mean zero
                    double v = filterRand.nextGaussian() * stdDev;
                    String val = fmtTwo.format(v);
                    // allow user to edit filter values
                    TextField t = new TextField(val);
                    t.setStyle("-fx-border-style: solid; -fx-border-width: 1; -fx-border-color: gray;");
                    t.setPrefColumnCount(FIELD_SIZE);
                    // add text to grid
                    setCell(i, j, t);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }


    /**
     * Get filter matrix format
     *
     * @return matrix filter
     */
    public Matrix getFilterMatrix() {
        Matrix filterMatrix = null;
        try {
            filterMatrix = new Matrix(rows, cols);
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    // must cast Node to same type used in InitFilter
                    TextField t = (TextField) getCell(i, j);
                    String s = t.getText();
                    double val = 0.0;
                    if(!s.isBlank()) {
                        val = Double.parseDouble(s);
                    }
                    MTX.setCell(filterMatrix, i, j, val);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return filterMatrix;
    }

    /**
     * Set this filter from matrix with same number of rows and columns
     *
     * @param filterMatrix a filter matrix
     */
    public void setFilterMatrix(Matrix filterMatrix) {
        try {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    double d = MTX.getCell(filterMatrix, i, j);
                    String s = fmtTwo.format(d);
                    // allow user to edit filter values
                    TextField t = new TextField(s);
                    t.setStyle("-fx-border-style: solid; -fx-border-width: 1; -fx-border-color: gray;");
                    t.setPrefColumnCount(FIELD_SIZE);
                    setCell(i, j, t);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

}  //end class