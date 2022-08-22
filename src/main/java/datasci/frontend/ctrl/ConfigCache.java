package datasci.frontend.ctrl;

import datasci.backend.model.NetConfig;

/**
 * Singleton class for storing shared data
 */
public class ConfigCache {

    //
    /**
     * Network config data store
     */
    private NetConfig config;

    private final static ConfigCache INSTANCE = new ConfigCache();

    private ConfigCache() {
    }

    public static ConfigCache getInstance() {
        return INSTANCE;
    }

    public NetConfig getConfig() {
        return config;
    }

    public void setConfig(NetConfig config) {
        this.config = config;
    }
}
