package datasci.frontend.util;

import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.util.concurrent.atomic.AtomicBoolean;

public class Cert {


    public static AtomicBoolean validateIntField(TextField intField) {
        AtomicBoolean isValid = new AtomicBoolean();
            try {
                int n = Integer.parseInt(intField.getText());
                // no exception above means valid integer
                intField.setBorder(null);
                isValid.set(true);
            } catch (NumberFormatException e) {
                // exception means input error: not integer
                // set border to red color
                intField.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(2), new Insets(-2))));
                isValid.set(false);
            }
        return isValid;
    }

    public static void checkIntField(TextField field) {
        //
        field.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter()));
        //
        field.textProperty().addListener((obs, oldval, newval) ->
        {
            try {
                field.getTextFormatter().getValueConverter().fromString(newval);
                // no exception above means valid integer
                field.setBorder(null);
            } catch (NumberFormatException e) {
                // exception means input error: not integer
                // set border to red color
                field.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(2), new Insets(-2))));
            }
        });
    }
    public static void checkDblField(TextField field) {
        //
        field.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter()));
        //
        field.textProperty().addListener((obs, oldval, newval) ->
        {
            try {
                field.getTextFormatter().getValueConverter().fromString(newval);
                // no exception above means valid double
                field.setBorder(null);
            } catch (NumberFormatException e) {
                // exception means input error: not double
                // set border to red color
                field.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(2), new Insets(-2))));
            }
        });
    }

    public static AtomicBoolean validateDoubleField(TextField dblField) {
        AtomicBoolean isValid = new AtomicBoolean();
            try {
                double d = Double.parseDouble(dblField.getText());
                // no exception above means valid double value
                dblField.setBorder(null);
                isValid.set(true);
            } catch (NumberFormatException e) {
                // exception means input error: not double
                // set border to red color
                dblField.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(2), new Insets(-2))));
                isValid.set(false);
            }
            return isValid;
    }

}
