package datasci.backend.model;

import java.util.ArrayList;
import java.util.List;

public class NetConfig {
    //
    public GeneralConfig generalConfig;
    //
    public InputConfig inputConfig;
    //
    public List<ConvoPoolConfig> convoPoolList = new ArrayList<>();
    //
    public List<InternalConfig> internalList = new ArrayList<>();
    //
    public OutputConfig outputConfig;


    public NetConfig() {

    }

}  // end class
