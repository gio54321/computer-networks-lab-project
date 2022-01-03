package winsome.common.responses;

public class LoginResponse extends Response {
    public String authToken;

    public LoginResponse() {
    }

    public LoginResponse(String authToken) {
        this.authToken = authToken;
    }
}
