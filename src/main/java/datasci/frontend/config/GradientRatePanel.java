package datasci.frontend.config;

import datasci.frontend.util.Cert;
import datasci.backend.model.EtaModel;
import datasci.frontend.util.ViewUtil;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GradientRatePanel {

    private static final Logger LOG = Logger.getLogger(GradientRatePanel.class.getName());
    // gradient descent rate model;
    private EtaModel rateModel;
    private final TextField minRateField = new TextField();
    private final TextField maxRateField = new TextField();
    private final TextField stepField = new TextField();
    private final TextField decayField = new TextField();
    private final RadioButton btnTriangle = new RadioButton("Triangle Decay");
    private final RadioButton btnStep = new RadioButton("Step Decay");
    private final ToggleGroup groupRateFn = new ToggleGroup();
    //
    public static final DecimalFormat SIZE_FMT = new DecimalFormat("##0.0#####");


    public GradientRatePanel() {
    }

    /**
     * Form for network configuration parameters
     *
     * @return pane with general configuration fields
     */
    public Pane init() {
        GridPane grid = new GridPane();
        try {
            grid.setVgap(10);
            grid.setHgap(10);
            int row = 0;
            //
            Label labelMinRate = new Label("Min descent rate:");
            // grid cell index: col, row
            grid.add(labelMinRate, 0, row);
            grid.add(minRateField, 1, row);
            Cert.checkDblField(minRateField);
            //
            Label labelMaxRate = new Label("Max descent rate:");
            // grid cell index: col, row
            grid.add(labelMaxRate, 0, ++row);
            grid.add(maxRateField, 1, row);
            Cert.checkDblField(maxRateField);
            //
            Label labelStep = new Label("Rate Decay step size:");
            // grid cell index: col, row
            grid.add(labelStep, 0, ++row);
            grid.add(stepField, 1, row);
            Cert.checkIntField(stepField);
            //
            Label labelDecay = new Label("Rate Decay per step:");
            // grid cell index: col, row
            grid.add(labelDecay, 0, ++row);
            grid.add(decayField, 1, row);
            Cert.checkDblField(decayField);
            //

            btnTriangle.setToggleGroup(groupRateFn);
            btnTriangle.setSelected(true);
            btnStep.setToggleGroup(groupRateFn);
            HBox btnRatePanel = new HBox(10);
            btnRatePanel.getChildren().addAll(btnTriangle, btnStep);
            ViewUtil.compact(btnRatePanel);
            Label labelRate = new Label("Rate Function:");
            // grid cell index: col, row
            grid.add(labelRate, 0, ++row);
            grid.add(btnRatePanel, 1, row);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return grid;
    }

    public EtaModel getRateModel(){
        EtaModel rateModel = new EtaModel();
        try{
            rateModel.minRate = Double.parseDouble(minRateField.getText());
            rateModel.maxRate = Double.parseDouble(maxRateField.getText());
            rateModel.stepCount = Integer.parseInt(stepField.getText());
            rateModel.decayPerStep = Double.parseDouble(decayField.getText());
            // rate function selected
            rateModel.rateFn = ((RadioButton)groupRateFn.getSelectedToggle()).getText();
            LOG.info("rateFn: " + rateModel.rateFn);
        } catch (Exception ex) {
        LOG.log(Level.SEVERE, ex.getMessage(), ex);
        throw new RuntimeException(ex);
    }
        return rateModel;

    }

    /**
     * Sets rate model.
     *
     * @param rateModel
     */
    public void setRateModel(EtaModel rateModel) {
        try {
            minRateField.setText(SIZE_FMT.format(rateModel.minRate));
            maxRateField.setText(SIZE_FMT.format(rateModel.maxRate));
            //
            stepField.setText(Integer.toString(rateModel.stepCount));
            decayField.setText(SIZE_FMT.format(rateModel.decayPerStep));
            //
            String rateFn = rateModel.rateFn;
            LOG.info("rateFn: " + rateFn);
            if(btnTriangle.getText().equalsIgnoreCase(rateFn)){
                groupRateFn.selectToggle(btnTriangle);
            } else if(btnStep.getText().equalsIgnoreCase(rateFn)){
                groupRateFn.selectToggle(btnStep);
            } else{
                String msg = "Invalid rate function: " + rateFn;
                LOG.log(Level.SEVERE, msg);
                throw new RuntimeException(msg);
            }
            //
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

}
