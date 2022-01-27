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
import winsome.server.database.serializables.SerializableDatabase;

public class ServerMain {
    private final static String CONFIG_PATH = "serverConfig.json";

    public static void main(String[] args) {
        try {

            // get the config entries
            var config = getServerConfig(CONFIG_PATH);

            // create the server's database
            var database = new Database(config.authorRewardCut);

            // attempt to load from saved database
            loadDbFromFile(database, config.databasePath);

            // create the multicast address and check that it is actually multicast
            var multicastAddress = InetAddress.getByName(config.multicastAddress);
            if (!multicastAddress.isMulticastAddress()) {
                System.out.println("ERROR: address " + config.multicastAddress + " is not multicast");
            }

            // create the reward calculator
            var rewardsCalculator = new RewardCalculator(database, config.rewardIntervalMillis, multicastAddress,
                    config.multicastPort);

            // create the persistence manager
            var persistenceManager = new PersistenceManager(database, config.persistenceIntervalMillis,
                    config.databasePath);

            // create the authentication interface object
            var auth = new AuthenticationImpl(database);

            // set up RMI followers callback and registration
            var followerCallbackService = setupRMI(database, auth, config.registryHostnName, config.registryPort);

            // create the rest logic object
            var logic = new RESTLogic(database, followerCallbackService);

            // give the rest logic the multicast information
            logic.setMulticastInformations(config.multicastAddress, config.multicastPort);

            // create the server's router
            var router = new Router(logic, auth);

            // create the REST server on the specified address and port
            var tcpAddress = new InetSocketAddress(config.serverAddress, config.serverPort);
            var RESTserver = new RESTServerManager(tcpAddress, router);

            // start the reward calculator
            rewardsCalculator.start();

            // start the persistence manager
            persistenceManager.start();

            // start the REST server
            RESTserver.serve();
        } catch (IOException | InvalidRouteAnnotationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Attempt to load database from file. If not successful, the database is
     * not modified
     * 
     * @param database the server's database
     * @param dbPath   the path to the json file
     */
    private static void loadDbFromFile(Database database, String dbPath) {
        try {
            var mapper = new ObjectMapper();
            var db = mapper.readValue(new File(dbPath), SerializableDatabase.class);
            if (db != null) {
                database.fromSerializable(db);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Attempt to get the server's config from path
     * 
     * @param configPath the path to the config
     * @return the deserialized config object
     * @throws IOException if the read was not successful
     */
    private static ServerConfig getServerConfig(String configPath)
            throws IOException {
        var mapper = new ObjectMapper();
        return mapper.readValue(new File(configPath), ServerConfig.class);
    }

    /**
     * Set up RMI component of the server, that is the calback service and the
     * registration service
     * 
     * @param database         the server's database
     * @param auth             the authentication interface
     * @param registryHostname the registry host name
     * @param registryPort     the registry port
     * @return the followers callback ervice object, null if some error occurred
     */
    private static FollowersCallbackServiceImpl setupRMI(Database database, AuthenticationInterface auth,
            String registryHostname, int registryPort) {
        // create the registration object
        var registrationImpl = new RegistrationImpl(database);

        // create the followers callback service object
        var followersCallbackImpl = new FollowersCallbackServiceImpl(auth, database);
        try {
            // export the registration object and the follower callback service object
            var registrationStub = (Registration) UnicastRemoteObject.exportObject(registrationImpl, 0);
            var followersCallbackStub = (FollowersCallbackService) UnicastRemoteObject
                    .exportObject(followersCallbackImpl, 0);

            // create the registry on localhost
            if (registryHostname.contentEquals("localhost")) {
                LocateRegistry.createRegistry(registryPort);
            }

            // get the registry
            var registry = LocateRegistry.getRegistry(registryHostname, registryPort);

            // bind the servicec to the registry
            registry.rebind("Registration-service", registrationStub);
            registry.rebind("FollowersCallback-service", followersCallbackStub);

            return followersCallbackImpl;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

}
