package winsome.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import winsome.common.rmi.Registration;

public class WinsomeConnection {
    private String winsomeServerHostname;
    private Registration registrationObj;

    public WinsomeConnection(String hostname) throws RemoteException, NotBoundException {
        if (hostname == null) {
            throw new NullPointerException();
        }
        this.winsomeServerHostname = hostname;

        // get the registration handler from registry
        // TODO host of registry
        // TODO port config
        var registryPort = 1234;
        var registry = LocateRegistry.getRegistry(registryPort);
        this.registrationObj = (Registration) registry.lookup("Registration-service");
    }

    public int register(String username, String password, String[] tags) {
        try {
            return this.registrationObj.registerToWinsome(username, password, tags);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
    }

}
