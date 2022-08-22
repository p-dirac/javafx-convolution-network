package datasci.frontend.util;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Observable properties model for gradient descent rate (eta)
 */
public class EtaModel {
    public int stepCount;
    public double decayPerStep;
    public double minRate;
    public double maxRate;
    public String rateFn;

    public EtaModel() {
    }


}
