package winsome.server;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;

import winsome.common.rmi.Registration;
import winsome.server.database.Database;
import winsome.server.database.User;
import winsome.server.database.exceptions.UserAlreadyExistsException;

/**
 * Implementation of the registration service
 */
public class RegistrationImpl extends RemoteServer implements Registration {

    private Database database;

    public RegistrationImpl(Database database) {
        if (database == null) {
            throw new NullPointerException();
        }

        this.database = database;
    }

    /**
     * Register an user to Winsome
     * 
     * @param username the username
     * @param password the password
     * @param tags     the list of tags. the length must be less or equal to 5
     * @throws RemoteException
     * @throws UserAlreadyExistsException
     */
    public void registerToWinsome(String username, String password, String[] tags)
            throws RemoteException, UserAlreadyExistsException {
        if (username == null || password == null || tags == null) {
            throw new NullPointerException();
        }

        // convert tags to lowercase
        for (int i = 0; i < tags.length; ++i) {
            tags[i] = tags[i].toLowerCase();
        }

        var user = new User(username, password, tags);
        this.database.registerUser(user);

        System.out.println("registered user " + username + " with " + tags.length + " tags");
    }
}
