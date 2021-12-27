package winsome.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import winsome.common.rmi.Registration;

public class ServerMain {
    public static void main(String[] args) {
        var usersDatabase = new UsersDatabase();

        // TODO port
        // TODO regostry host
        var registryPort = 1234;
        var registrationImpl = new RegistrationImpl(usersDatabase);
        try {
            var stub = (Registration) UnicastRemoteObject.exportObject(registrationImpl, 0);
            LocateRegistry.createRegistry(registryPort);
            var registry = LocateRegistry.getRegistry(registryPort);
            registry.rebind("Registration-service", stub);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        while (true)
            ;

    }

}
