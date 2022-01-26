package winsome.common.requests;

/*
 * Request model for login
 * Used for request body serialization
 */
public class LoginRequest extends Request {
    public String username;
    public String password;
}
