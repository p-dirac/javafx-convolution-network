package datasci.frontend.config;

import datasci.backend.activations.ActE;
import datasci.backend.model.InternalConfig;
import datasci.frontend.util.Cert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.logging.Level;
import java.util.logging.Logger;

public class InternalConfigPanel extends GridPane {
    private static final Logger LOG = Logger.getLogger(GeneralConfigPanel.class.getName());
    //
    private ChoiceBox<String> actChoices = new ChoiceBox<>();
    private TextField inNodeField = new TextField();
    private TextField outNodeField = new TextField();


    public InternalConfigPanel() {
    }

    /**
     * Form for internal layer configuration parameters
     *
     * @return pane with internal configuration fields
     */
    public void init() {
        try {
            this.setVgap(10);
            this.setHgap(10);
            // grid row index
            int row = 0;
            //
            Label internalLabel = new Label("Internal layer config:");
            // grid cell index: col, row, colSpan, rowSpan
            this.add(internalLabel, 0, row, 2, 1);

            //
            Label labelAct = new Label("Activation function name:");
            // grid cell index: col, row
            this.add(labelAct, 0, ++row);
            actChoices.getItems().setAll(ActE.getLabels());
            actChoices.getSelectionModel().select(ActE.LEAKY_RELU.label);
            this.add(actChoices, 1, row);
            //
            Label labelOutNode = new Label("Number of Output Nodes:");
            // grid cell index: col, row
            this.add(labelOutNode, 0, ++row);
            this.add(outNodeField, 1, row);
            Cert.checkIntField(outNodeField);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }
    public InternalConfig getConfig() {
        InternalConfig config = new InternalConfig();
        try {
            config.actName = (String)actChoices.getValue();
            if(Cert.validateIntField(outNodeField).get()) {
                config.numOutputNodes = Integer.parseInt(outNodeField.getText());
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return config;
    }

    public void setConfig(InternalConfig config) {
        try {
            actChoices.setValue(config.actName);
            outNodeField.setText(Integer.toString(config.numOutputNodes));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

}  //end class
