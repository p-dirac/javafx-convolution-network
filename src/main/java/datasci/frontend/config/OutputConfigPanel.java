package datasci.frontend.config;

import datasci.backend.activations.ActE;
import datasci.backend.model.OutputConfig;
import datasci.frontend.util.Cert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Form for output layer configuration parameters
 */
public class OutputConfigPanel {
    private static final Logger LOG = Logger.getLogger(OutputConfigPanel.class.getName());
    //
    private ChoiceBox<String> actChoices = new ChoiceBox<>();
    private TextField outNodeField = new TextField();

    public OutputConfigPanel() {
    }

    /**
     * Initialize form for output layer configuration parameters
     *
     * @return pane with output configuration fields
     */
    public Pane init() {
        GridPane grid = new GridPane();
        try {
            grid.setVgap(10);
            grid.setHgap(10);
            // grid row index
            int row = 0;
            //
            Label labelAct = new Label("Activation function name:");
            // grid cell index: col, row
            grid.add(labelAct, 0, row);
            actChoices.getItems().setAll(ActE.getLabels());
            actChoices.getSelectionModel().select(ActE.SOFTMAX.label);
            grid.add(actChoices, 1, row);
            //
            Label labelOutNode = new Label("Number of Output Nodes:");
            // grid cell index: col, row
            grid.add(labelOutNode, 0, ++row);
            grid.add(outNodeField, 1, row);
            Cert.checkIntField(outNodeField);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return grid;
    }
    public OutputConfig getConfig() {
        OutputConfig config = new OutputConfig();
        try {
            config.actName = (String)actChoices.getValue();
            //
            config.numOutputNodes = 0;
            if(Cert.validateIntField(outNodeField).get()) {
                config.numOutputNodes = Integer.parseInt(outNodeField.getText());
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return config;
    }

    public void setConfig(OutputConfig config) {
        try {
            actChoices.setValue(config.actName);
            outNodeField.setText(Integer.toString(config.numOutputNodes));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

}  //end class
