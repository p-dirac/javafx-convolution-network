package datasci.frontend.config;

import java.util.ArrayList;
import java.util.List;

public class NetConfigPanels {

    public GeneralConfigPanel generalPanel;
    //
    public InputConfigPanel inputPanel;
    //
    public List<ConvoPoolConfigPanel> convoPoolPanels = new ArrayList<>();
    //
    public List<InternalConfigPanel> internalPanels = new ArrayList<>();
    //
    public OutputConfigPanel outputPanel;

    public NetConfigPanels() {
    }
}
