package winsome.common.responses;

public class UserResponse {
    public String username;
    public String[] tags;

    public UserResponse() {
    }

    public UserResponse(String username, String[] tags) {
        this.username = username;
        this.tags = tags;
    }
}
