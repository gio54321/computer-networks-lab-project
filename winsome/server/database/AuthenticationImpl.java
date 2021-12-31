package winsome.server.database;

import winsome.lib.router.AuthenticationInterface;

public class AuthenticationImpl implements AuthenticationInterface {
    private Database database;

    public AuthenticationImpl(Database database) {
        this.database = database;
    }

    public boolean authenticateUser(String username, String authToken) {
        return this.database.authenticateUser(username, authToken);
    }

}
