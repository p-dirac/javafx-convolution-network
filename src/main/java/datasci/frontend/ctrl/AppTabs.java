package datasci.frontend.ctrl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application tabs
 *
 * @author r cook
 */
public class AppTabs {


    //private static final Logger LOG = Logger.getLogger(AppTabs.class.getName());
    private static Logger LOG = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    //
    private BorderPane rootPane;
    private final TabPane appTabPane = new TabPane();
    private final ObservableList<Tab> tabList = FXCollections.observableArrayList();

    public AppTabs() {
    }

    public void initPanel(BorderPane rootPane) {
        try {
            this.rootPane = rootPane;
            appTabPane.getTabs().addAll(tabList);
            rootPane.setCenter(appTabPane);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    public TabPane getAppTabPane() {
        return appTabPane;
    }

    public ObservableList<Tab> getTabList() {
        return tabList;
    }

    public SingleSelectionModel<Tab> getSelectionModel() {
        return appTabPane.getSelectionModel();
    }

    /**
     * Get existing tab by name
     *
     * @param name
     * @return Tab found by name or null
     */
    public Tab getTabByName(String name) {
        Tab tab = null;
        try {
            if (!tabList.isEmpty()) {
                for (Tab t : tabList) {
                    if (t.getText().equalsIgnoreCase(name)) {
                        tab = t;
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return tab;
    }

    /**
     * Add new tab
     *
     * @param tabName name of tab
     */
    public Tab addTab(String tabName) {
        Tab tab = null;
        try {
         //   LOG.log(Level.INFO, "add tab: " + tabName);
            tab = new Tab();
            tab.setText(tabName);
            tab.setContent(new BorderPane());
            tab.setClosable(false);
            tabList.add(tab);
            appTabPane.getTabs().add(tab);
            //
            appTabPane.getSelectionModel().select(tab);
            LOG.log(Level.INFO, "addTab, select: " + tabName);
            //
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return tab;
    }

    /**
     * Add new tab or select specified tab
     *
     * @param tabName name of tab
     */
    public Tab addOrSelectTab(String tabName) {
        Tab tab = null;
        try {
            Tab t = getTabByName(tabName);
            if(t != null){
                tab = t;
                appTabPane.getSelectionModel().select(tab);
            } else{
                tab = addTab(tabName);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return tab;
    }

} // end class