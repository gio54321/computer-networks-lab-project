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

    private static void dummyDb(Database db) {
        try {
            String[] tags1 = { "sports", "art", "music" };
            String[] tags2 = { "art", "rock" };
            String[] tags3 = { "programming", "rock" };
            String[] tags4 = { "buisness", "music", "art" };

            var user1 = new User("user1", "pass1", tags1);
            var user2 = new User("user2", "pass2", tags2);
            var user3 = new User("aaaaaaaaaaa", "pass3", tags3);
            var user4 = new User("f", "pass4", tags4);
            var user5 = new User("a", "a", tags1);

            db.registerUser(user1);
            db.registerUser(user2);
            db.registerUser(user3);
            db.registerUser(user4);
            db.registerUser(user5);
        } catch (UserAlreadyExistsException e) {
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
