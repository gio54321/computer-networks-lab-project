package winsome.common.responses;

/*
 * Response model for login response (authToken)
 * Used for response body serialization
 */
public class LoginResponse extends ResponseModel {
    public String authToken;

    public LoginResponse() {
    }

    public LoginResponse(String authToken) {
        this.authToken = authToken;
    }
}
