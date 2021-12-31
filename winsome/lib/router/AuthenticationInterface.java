package winsome.lib.router;

public interface AuthenticationInterface {
    public boolean authenticateUser(String username, String authToken);
}
