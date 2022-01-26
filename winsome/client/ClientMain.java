package winsome.client;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientMain {
    public static void main(String[] args) {
        try {
            var config = getClientConfig("clientConfig.json");

            var addr = InetAddress.getByName(config.serverAddress);
            var connection = new WinsomeConnection(addr, config.serverPort, config.registryHostnName,
                    config.registryPort, config.netIfName);
            var cli = new CommandLineInterface(connection);
            cli.runInterpreter();
        } catch (RemoteException | NotBoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("fjdklsfkjsal");

    }

    private static ClientConfig getClientConfig(String configPath) {
        try {
            var mapper = new ObjectMapper();
            return mapper.readValue(new File(configPath), ClientConfig.class);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
}
