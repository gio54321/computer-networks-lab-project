package winsome.server;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;

import winsome.common.rmi.Registration;
import winsome.server.database.Database;
import winsome.server.database.User;
import winsome.server.database.exceptions.UserAlreadyExistsException;

public class RegistrationImpl extends RemoteServer implements Registration {

    private Database database;

    public RegistrationImpl(Database database) {
        if (database == null) {
            throw new NullPointerException();
        }

        this.database = database;
    }

    public void registerToWinsome(String username, String password, String[] tags)
            throws RemoteException, UserAlreadyExistsException {
        if (username == null || password == null || tags == null) {
            throw new NullPointerException();
        }

        var user = new User(username, password, tags);
        this.database.registerUser(user);

        System.out.println("registered");
    }
}
