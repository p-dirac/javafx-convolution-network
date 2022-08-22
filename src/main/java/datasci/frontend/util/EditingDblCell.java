package datasci.frontend.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;

import java.util.logging.Logger;

/**
 * TableCell is an example of poorly designed JavaFX code. The reference below shows various workarounds.
 *
 * Ref: https://stackoverflow.com/questions/29576577/tableview-doesnt-commit-values-on-focus-lost-event
 *
 * TableCell<S,T>
 *     S - The type of the TableView generic type. This should also match with the first generic type in TableColumn.
 *     T - The type of the item contained within the Cell.
 *
 *  Note: this implementation keeps the cell value as edited, and there is no cancel edit feature.
 */
public class EditingDblCell extends TableCell<Object, Double> {
    private static final Logger LOG = Logger.getLogger(EditingDblCell.class.getName());

        private TextField textField;

        public EditingDblCell() {
        }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.selectAll();
            }
        }

        @Override
        public void cancelEdit() {
            // avoid JavaFX NullPointerException when calling commitEdit()
            getTableView().edit(getIndex(), getTableColumn());
            // Redirects to commitEdit, not cancel
            commitEdit(Double.parseDouble(textField.getText()));
        }

        @Override
        public void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);
            //
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
            textField.focusedProperty().addListener(new ChangeListener<Boolean>(){
                @Override
                public void changed(ObservableValue<? extends Boolean> arg0,
                                    Boolean arg1, Boolean arg2) {
                    if (!arg2) {
          //              LOG.info("call commitEdit: " + textField.getText());
                        commitEdit(Double.parseDouble(textField.getText()));
                    }
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }

