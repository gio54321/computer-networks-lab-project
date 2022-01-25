package winsome.server;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import winsome.common.rmi.FollowersCallbackService;
import winsome.common.rmi.Registration;
import winsome.lib.router.AuthenticationInterface;
import winsome.lib.router.InvalidRouteAnnotationException;
import winsome.lib.router.Router;
import winsome.server.database.AuthenticationImpl;
import winsome.server.database.Database;
import winsome.server.database.serializables.SerializableDatabase;

public class ServerMain {
    public static void main(String[] args) {
        try {
            var database = new Database();
            loadDbFromFile(database);
            // TODO settings
            var rewardsCalculator = new RewardCalculator(10000, database);
            var persistenceManager = new PersistenceManager(database, 5000);
            var auth = new AuthenticationImpl(database);
            var followerCallbackService = setupRMI(database, auth);
            var logic = new RESTLogic(database, followerCallbackService);
            var router = new Router(logic, auth);
            var RESTserver = new RESTServerManager(new InetSocketAddress(1234), router);

            rewardsCalculator.start();
            persistenceManager.start();
            RESTserver.serve();
        } catch (IOException | InvalidRouteAnnotationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void loadDbFromFile(Database database) {
        var mapper = new ObjectMapper();
        try {
            SerializableDatabase db = mapper.readValue(new File("init_db.json"), SerializableDatabase.class);
            database.fromSerializable(db);
        } catch (JsonParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static FollowersCallbackServiceImpl setupRMI(Database database, AuthenticationInterface auth) {

        // TODO port
        // TODO regostry host
        var registryPort = 1235;
        var registrationImpl = new RegistrationImpl(database);
        var followersCallbackImpl = new FollowersCallbackServiceImpl(auth, database);
        try {
            var registrationStub = (Registration) UnicastRemoteObject.exportObject(registrationImpl, 0);
            var followersCallbackStub = (FollowersCallbackService) UnicastRemoteObject
                    .exportObject(followersCallbackImpl, 0);

            LocateRegistry.createRegistry(registryPort);
            var registry = LocateRegistry.getRegistry(registryPort);

            registry.rebind("Registration-service", registrationStub);
            registry.rebind("FollowersCallback-service", followersCallbackStub);

            return followersCallbackImpl;
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}
