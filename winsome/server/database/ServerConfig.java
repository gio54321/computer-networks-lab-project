package winsome.server.database;

public class ServerConfig {
    public String databasePath;

    public String serverIp;
    public int serverPort;

    public String multicastAddress;
    public int multicastPort;

    public String registryHostnName;
    public int registryPort;

    public long rewardIntervalMillis;
    public long persistenceIntervalMillis;

    public double authorRewardCut;
}
