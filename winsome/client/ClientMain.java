package winsome.client;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.NotBoundException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientMain {
    public static void main(String[] args) {
        // get the config file path from the first argument
        if (args.length != 1) {
            System.out.println("Usage: java ClientMain configFile");
        }

        var configFilePath = args[0];

        try {
            // try to read the configuration file
            var config = getClientConfig(configFilePath);
            if (config == null) {
                return;
            }

            // get the server address
            var addr = InetAddress.getByName(config.serverAddress);
            // instantiate a new winsome connection
            var connection = new WinsomeConnection(addr, config.serverPort, config.registryHostnName,
                    config.registryPort, config.netIfName);

            // create a new command line interface with given connection
            var cli = new CommandLineInterface(connection);
            // run the command line interpreter
            cli.runInterpreter();
        } catch (IOException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Try to read the client configuration file and returns a ClientConfig
     * 
     * @param configPath the path to the config file
     * @return null if the read was not successful, the read ClientConfig object
     *         otherwise
     */
    private static ClientConfig getClientConfig(String configPath) {
        if (configPath == null) {
            throw new NullPointerException();
        }
        try {
            var mapper = new ObjectMapper();
            return mapper.readValue(new File(configPath), ClientConfig.class);
        } catch (IOException e) {
            System.out.println("Unable to read client configuration file");
            e.printStackTrace();
            return null;
        }
    }
}
