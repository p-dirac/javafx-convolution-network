package datasci.frontend.config;

import datasci.backend.activations.ActE;
import datasci.backend.model.ConvoConfig;
import datasci.backend.model.PoolConfig;
import datasci.frontend.util.Cert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel for setting convolution and pool layer parameters
 *
 * Note:
 *  convolution layer properties
 *  (Rows, columns) of input matrix = (n, n)
 *  (Rows, columns) of filter matrix = (f,f)
 *  (Rows, columns) of convolution output matrix = ((n-f+1) , (n-f+1))
 *  There may be a list of filter matrix for each input matrix
 */
public class ConvoPoolConfigPanel {
    private static final Logger LOG = Logger.getLogger(ConvoPoolConfigPanel.class.getName());
    //
    private final ChoiceBox<String> actConvoChoices = new ChoiceBox<>();
    private final TextField filterSizeField = new TextField();
    private final TextField numFiltersField = new TextField();
    //
    private final ChoiceBox<String> actPoolChoices = new ChoiceBox<>();
    private final TextField poolSizeField = new TextField();


    public ConvoPoolConfigPanel() {
    }

    /**
     * Form for internal layer configuration parameters
     *
     * @return pane with internal configuration fields
     */
    public Pane createConvoPane() {
        // new vbox for grid and button bar
        VBox convo = new VBox(10);
        try {
            GridPane grid = new GridPane();
            grid.setVgap(10);
            grid.setHgap(10);
            // grid row index
            int row = 0;
            //
            Label convoLabel = new Label("Convolution layer config:");
            // grid cell index: col, row, colSpan, rowSpan
            grid.add(convoLabel, 0, row, 2, 1);
            //
            Label labelAct = new Label("Convolution Activation function name:");
            // grid cell index: col, row
            grid.add(labelAct, 0, ++row);
            actConvoChoices.getItems().setAll(ActE.getLabels());
            actConvoChoices.getSelectionModel().select(ActE.IDENT.label);
            grid.add(actConvoChoices, 1, row);

            //
            Label labelFilterSize = new Label("Filter Size:");
            // grid cell index: col, row
            grid.add(labelFilterSize, 0, ++row);
            grid.add(filterSizeField, 1, row);
            Cert.checkIntField(filterSizeField);
            //
            Label labelNum = new Label("Number of Filters:");
            // grid cell index: col, row
            grid.add(labelNum, 0, ++row);
            grid.add(numFiltersField, 1, row);
            Cert.checkIntField(numFiltersField);
            //

            //
            // add grid to vbox, which may contain several filter layer config grids
            convo.getChildren().add(grid);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return convo;
    }

    /**
     * Form for internal layer configuration parameters
     *
     * @return pane with internal configuration fields
     */
    public Pane createPoolPane() {
        GridPane grid = new GridPane();
        try {
            grid.setVgap(10);
            grid.setHgap(10);
            // grid row index
            int row = 0;
            //
            Label poolLabel = new Label("Pool layer config:");
            // grid cell index: col, row, colSpan, rowSpan
            grid.add(poolLabel, 0, row, 2, 1);

            //
            Label labelAct = new Label("Pool Activation function name:");
            // grid cell index: col, row
            grid.add(labelAct, 0, ++row);
        //    actPoolChoices.getItems().setAll(ActE.getLabels());
            actPoolChoices.getItems().setAll(ActE.NONE.label);
            actPoolChoices.getSelectionModel().select(ActE.NONE.label);
            grid.add(actPoolChoices, 1, row);
            //
            Label labelSize = new Label("Pool Size:");
            // grid cell index: col, row
            grid.add(labelSize, 0, ++row);
            grid.add(poolSizeField, 1, row);
            Cert.checkIntField(poolSizeField);
            //
            //
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return grid;
    }

    private boolean validateFilterFields(){
        boolean isOk = false;
        try{
            AtomicBoolean isValid1 = Cert.validateIntField(filterSizeField);
            AtomicBoolean isValid2 = Cert.validateIntField(numFiltersField);
            //
            if(isValid1.get() && isValid2.get()) {
                isOk = true;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return isOk;
    }
    //

    public ConvoConfig getConvoConfig() {
        ConvoConfig config = new ConvoConfig();
        try {
            config.actName = (String) actConvoChoices.getValue();
            //
            // allow zero to edit later ?
            config.filterSize = 0;
            config.numFilters = 0;
            if(validateFilterFields()) {
                config.filterSize = Integer.parseInt(filterSizeField.getText());
                config.numFilters = Integer.parseInt(numFiltersField.getText());
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return config;
    }

    public void setConvoConfig(ConvoConfig config) {
        try {
            actConvoChoices.setValue(config.actName);
            filterSizeField.setText(Integer.toString(config.filterSize));
            numFiltersField.setText(Integer.toString(config.numFilters));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }


    public PoolConfig getPoolConfig() {
        PoolConfig config = new PoolConfig();
        try {
            config.actName = (String) actPoolChoices.getValue();
            //
            // allow zero to edit later ?
            config.poolSize = 0;
            if(Cert.validateIntField(poolSizeField).get()) {
                config.poolSize = Integer.parseInt(poolSizeField.getText());
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return config;
    }

    public void setPoolConfig(PoolConfig config) {
        try {
            actPoolChoices.setValue(config.actName);
            poolSizeField.setText(Integer.toString(config.poolSize));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

}  // end class
