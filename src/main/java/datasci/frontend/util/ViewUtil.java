package datasci.frontend.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility methods for user interface panels
 */
public class ViewUtil {
    private static final Logger LOG = Logger.getLogger(ViewUtil.class.getName());

    /**
     * Calc wh width height.
     *
     * @param node the node
     * @return the width height
     */
    public static WidthHeightR calcWH(Node node) {
        // must not set record here, since it cannot be changed
        WidthHeightR wh = null;
        try {
            // need temp Scene, applyCss, layout for getLayoutBounds to work
            Group g = new Group();
            g.getChildren().add(node);
            new Scene(g);
            node.applyCss();
            g.layout();
            // node cannot be empty;
            // node must contain something with explicit or implicit size, such as string, text
            double w = node.getBoundsInLocal().getWidth();
            double h = node.getBoundsInLocal().getHeight();
            // set record here, since it cannot be changed
            wh = new WidthHeightR(w, h);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return wh;
    }

    /**
     * Compact.
     *
     * @param pane the pane
     */
    public static void compact(Region pane) {
        try {
            pane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            pane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            pane.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }
    /**
     * Compact.
     *
     * @param pane the pane
     */
    public static void compactMin(Region pane) {
        try {
            pane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            pane.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Compact.
     *
     * @param pane the pane
     */
    public static void compactW(Region pane) {
        try {
            pane.setPrefWidth(Region.USE_COMPUTED_SIZE);
            pane.setMaxWidth(Region.USE_PREF_SIZE);
            pane.setMinWidth(Region.USE_PREF_SIZE);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Rotate Label
     */
    public static StackPane rotateLabel90(Label label) {
        StackPane stp = new StackPane(new Group(label));
        try {
            label.setRotate(90);
            stp.setRotate(90);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return stp;
    }
    /**
     * Get menu by name
     *
     * @param menuBar  menu bar that contains menu
     * @param menuName name of menu
     * @return Menu
     */
    public static Menu getMenuByName(MenuBar menuBar, String menuName) {
        Menu menu = null;
        try {
            ObservableList<Menu> menuList = menuBar.getMenus();
            for (Menu m : menuList) {
                String name = m.getText();
                if (name.equalsIgnoreCase(menuName)) {
                    menu = m;
                    break;
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return menu;
    }

    /**
     * Get menu item by name
     *
     * @param menu     menu that contains menu item
     * @param itemName name of menu item
     * @return MenuItem
     */
    public static MenuItem getMenuItmeByName(Menu menu, String itemName) {
        MenuItem item = null;
        try {
            ObservableList<MenuItem> itemList = menu.getItems();
            for (MenuItem i : itemList) {
                String name = i.getText();
                if (name.equalsIgnoreCase(itemName)) {
                    item = i;
                    break;
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return item;
    }

    public static void waitForScrollSkin(ScrollPane scroller, double w) {
        if (scroller.getSkin() == null) {
            // Skin not yet attached, wait until skin is attached
            ChangeListener<Skin<?>> skinChangeListener = new ChangeListener<Skin<?>>() {
                @Override
                public void changed(ObservableValue<? extends Skin<?>> observable, Skin<?> oldValue, Skin<?> newValue) {
                    scroller.skinProperty().removeListener(this);
                    setScrollBarW(scroller, w);
                }
            };
            scroller.skinProperty().addListener(skinChangeListener);
        } else {
            // Skin already attached, access the scroll bars
            setScrollBarW(scroller, w);
        }
    }
    public static void setScrollBarW(ScrollPane scroller, double w) {
        Set<Node> nodes = scroller.lookupAll(".scroll-bar");
     //   LOG.log(Level.INFO, "ScrollBar nodes: " + nodes + ", w: " + w);
        for (final Node node : nodes) {
            if (node instanceof ScrollBar) {
                ScrollBar sb = (ScrollBar) node;
             //   LOG.log(Level.INFO, "ScrollBar sb: " + sb + ", w: " + w);
                if (sb.getOrientation() == Orientation.VERTICAL) {
                    // define preferred width
                    sb.setPrefWidth(w);
                }
            }
        }
    }

    public static File openFileDialog(String title) {
        File file = null;
        try {
            FileChooser fileDialog = new FileChooser();
            fileDialog.setTitle(title);
            // Show dialog
            file = fileDialog.showOpenDialog(null);
            if (file != null) {
                LOG.log(Level.INFO, "openFileDialog, file: " + file.getName());
            }
        } catch (Exception ex) {
            String e = ex.getMessage();
            if (e.contains("it is being used")) {
                if (file != null) {
                    String filename = file.getName();
                    LOG.log(Level.SEVERE, "File: " + filename + " is in use; cannot open.");
                } else {
                    LOG.log(Level.SEVERE, "File is in use; cannot open.");
                }
            } else {
                LOG.log(Level.SEVERE, ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        }
        return file;
    }

    public static File openCreateFileDialog(String title) {
        File file = null;
        try {
            FileChooser fileDialog = new FileChooser();
            fileDialog.setTitle(title);
            // Show dialog
            file = fileDialog.showSaveDialog(null);
            if (file != null) {
                LOG.log(Level.INFO, "openFileDialog, file: " + file.getName());
            }
        } catch (Exception ex) {
            String e = ex.getMessage();
            if (e.contains("it is being used")) {
                if (file != null) {
                    String filename = file.getName();
                    LOG.log(Level.SEVERE, "File: " + filename + " is in use; cannot open.");
                } else {
                    LOG.log(Level.SEVERE, "File is in use; cannot open.");
                }
            } else {
                LOG.log(Level.SEVERE, ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        }
        return file;
    }

}  //end class
