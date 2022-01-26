package winsome.server.database;

import winsome.lib.router.AuthenticationInterface;

/**
 * Actual implementation of the autheitication interface, used
 * to give the router an authInterface
 */
public class AuthenticationImpl implements AuthenticationInterface {
    private Database database;

    public AuthenticationImpl(Database database) {
        if (database == null) {
            throw new NullPointerException();
        }
        this.database = database;
    }

    /**
     * Authenticate a user
     * 
     * @param username  the username
     * @param authToken the authToken
     * @return true if username/authToken are a valid authentication
     */
    public boolean authenticateUser(String username, String authToken) {
        return this.database.authenticateUser(username, authToken);
    }
}
