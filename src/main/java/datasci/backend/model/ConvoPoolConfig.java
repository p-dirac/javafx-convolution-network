package datasci.backend.model;

public class ConvoPoolConfig {

    public ConvoConfig convoConfig;
    public PoolConfig poolConfig;

    public ConvoPoolConfig() {
    }

    public ConvoPoolConfig(ConvoConfig convoConfig, PoolConfig poolConfig) {
        this.convoConfig = convoConfig;
        this.poolConfig = poolConfig;
    }
}
