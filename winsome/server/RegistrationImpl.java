package winsome.server;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;

import winsome.common.rmi.Registration;

public class RegistrationImpl extends RemoteServer implements Registration {

    private UsersDatabase database;

    public RegistrationImpl(UsersDatabase database) {
        if (database == null) {
            throw new NullPointerException();
        }

        this.database = database;
    }

    public int registerToWinsome(String username, String password, String[] tags) throws RemoteException {
        // TODO check if the username already exists
        var user = new User(username, password, tags);
        var newUserId = this.database.addNewUserToDatabase(user);

        System.out.println("registered");
        System.out.println(newUserId);

        return newUserId;
    }
}
