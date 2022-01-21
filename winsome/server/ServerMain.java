package winsome.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import winsome.common.rmi.Registration;
import winsome.lib.router.InvalidRouteAnnotationException;
import winsome.lib.router.Router;
import winsome.server.database.AuthenticationImpl;
import winsome.server.database.Database;

public class ServerMain {
    public static void main(String[] args) {
        try {
            var database = new Database();
            setupRMI(database);
            var logic = new RESTLogic(database);
            var auth = new AuthenticationImpl(database);
            var router = new Router(logic, auth);
            var RESTserver = new RESTServerManager(new InetSocketAddress(1234), router);
            RESTserver.serve();
        } catch (IOException | InvalidRouteAnnotationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void setupRMI(Database database) {

        // TODO port
        // TODO regostry host
        var registryPort = 1235;
        var registrationImpl = new RegistrationImpl(database);
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
