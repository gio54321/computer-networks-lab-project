package winsome.client;

/**
 * Class that describes the type of client configuration json.
 * Used for deserialization from the client config file
 */
public class ClientConfig {
    // server address
    public String serverAddress;
    // server port
    public int serverPort;

    // RMI registry host name
    public String registryHostnName;
    // RMI registry port
    public int registryPort;

    // network interface name for multicast group joining
    public String netIfName;
}
