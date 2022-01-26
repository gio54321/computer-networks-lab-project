package winsome.server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import winsome.common.rmi.FollowersCallbackService;
import winsome.common.rmi.Registration;
import winsome.lib.router.AuthenticationInterface;
import winsome.lib.router.InvalidRouteAnnotationException;
import winsome.lib.router.Router;
import winsome.server.database.AuthenticationImpl;
import winsome.server.database.Database;
import winsome.server.database.ServerConfig;
import winsome.server.database.serializables.SerializableDatabase;

public class ServerMain {
    public static void main(String[] args) {
        try {
            var config = getServerConfig("serverConfig.json");

            var database = new Database(config.authorRewardCut);
            loadDbFromFile(database, config.databasePath);

            var multicastAddress = InetAddress.getByName(config.multicastAddress);
            var rewardsCalculator = new RewardCalculator(database, config.rewardIntervalMillis, multicastAddress,
                    config.multicastPort);
            var persistenceManager = new PersistenceManager(database, config.persistenceIntervalMillis,
                    config.databasePath);

            var auth = new AuthenticationImpl(database);
            var followerCallbackService = setupRMI(database, auth, config.registryHostnName, config.registryPort);
            var logic = new RESTLogic(database, followerCallbackService);

            // give the rest logic the multicast information
            logic.setMulticastInformations(config.multicastAddress, config.multicastPort);

            var router = new Router(logic, auth);
            var tcpAddress = new InetSocketAddress(config.serverAddress, config.serverPort);
            var RESTserver = new RESTServerManager(tcpAddress, router);

            rewardsCalculator.start();
            persistenceManager.start();
            RESTserver.serve();
        } catch (IOException | InvalidRouteAnnotationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void loadDbFromFile(Database database, String dbPath) {
        try {
            var mapper = new ObjectMapper();
            var db = mapper.readValue(new File(dbPath), SerializableDatabase.class);
            if (db != null) {
                database.fromSerializable(db);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static ServerConfig getServerConfig(String configPath) {
        try {
            var mapper = new ObjectMapper();
            return mapper.readValue(new File(configPath), ServerConfig.class);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    private static FollowersCallbackServiceImpl setupRMI(Database database, AuthenticationInterface auth,
            String registryHostname, int registryPort) {
        var registrationImpl = new RegistrationImpl(database);
        var followersCallbackImpl = new FollowersCallbackServiceImpl(auth, database);
        try {
            var registrationStub = (Registration) UnicastRemoteObject.exportObject(registrationImpl, 0);
            var followersCallbackStub = (FollowersCallbackService) UnicastRemoteObject
                    .exportObject(followersCallbackImpl, 0);

            LocateRegistry.createRegistry(registryPort);
            var registry = LocateRegistry.getRegistry(registryHostname, registryPort);

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
