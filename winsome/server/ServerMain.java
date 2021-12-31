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
import winsome.server.database.User;
import winsome.server.database.exceptions.UserAlreadyExistsException;

public class ServerMain {
    public static void main(String[] args) {
        try {
            var database = new Database();

            dummyDb(database);

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

    public static void dummyDb(Database db) {
        String[] tags = { "sports" };
        var user1 = new User("gio", "aaa", tags);
        var user2 = new User("tom", "bbb", tags);
        try {
            db.registerUser(user1);
            db.registerUser(user2);
        } catch (UserAlreadyExistsException e) {
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
