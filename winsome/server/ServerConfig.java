package winsome.server;

/**
 * Class that describes the type of server configuration json.
 * Used for deserialization from the server config file
 */
public class ServerConfig {
    public String databasePath;

    public String serverAddress;
    public int serverPort;

    public String multicastAddress;
    public int multicastPort;

    public String registryHostnName;
    public int registryPort;

    public long rewardIntervalMillis;
    public long persistenceIntervalMillis;

    public double authorRewardCut;
}
