package winsome.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import winsome.common.rmi.Registration;
import winsome.lib.router.InvalidRouteAnnotationException;
import winsome.server.database.Database;

public class ServerMain {
    public static void main(String[] args) {
        try {
            var database = new Database();
            var logic = new RESTLogic(database);
            var RESTserver = new RESTServerManager(new InetSocketAddress(1234), logic);
            RESTserver.serve();
        } catch (IOException | InvalidRouteAnnotationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void setupRMI() {

        var usersDatabase = new Database();

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
    }

}
