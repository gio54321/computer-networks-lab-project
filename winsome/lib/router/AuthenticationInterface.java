package winsome.lib.router;

/**
 * Interface that represent an object that provide user authentication
 */
public interface AuthenticationInterface {
    /**
     * Authenticate a user
     * 
     * @param username  the username
     * @param authToken the authToken
     * @return true if username/authToken are a valid authentication
     */
    public boolean authenticateUser(String username, String authToken);
}
