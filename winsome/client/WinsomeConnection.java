package winsome.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import winsome.common.rmi.Registration;
import winsome.server.database.exceptions.UserAlreadyExistsException;

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

    public void register(String username, String password, String[] tags) {
        try {
            this.registrationObj.registerToWinsome(username, password, tags);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UserAlreadyExistsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
