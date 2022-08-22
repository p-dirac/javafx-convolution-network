package datasci.frontend.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;

import java.util.logging.Logger;

/**
 * TableCell<S,T>
 *     S - The type of the TableView generic type. This should also match with the first generic type in TableColumn.
 *     T - The type of the item contained within the Cell.
 */
public class EditingCell extends TableCell<Object, Object> {
    private static final Logger LOG = Logger.getLogger(EditingCell.class.getName());

        private TextField textField;

        public EditingCell() {
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
            super.cancelEdit();
            //
            LOG.info("getItem string: " + getItem().toString());
            setText(getItem().toString());
            setGraphic(null);
        }

        @Override
        public void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            //
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        LOG.info("isEditing getString: " + getString());
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    LOG.info("else getString: " + getString());
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
                        LOG.info("call commitEdit: " + textField.getText());
                        commitEdit(textField.getText());
                    }
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }

