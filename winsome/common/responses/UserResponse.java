package winsome.common.responses;

import java.io.Serializable;

/*
 * Response model for user
 * Used for response body serialization
 */
public class UserResponse implements Serializable {
    public String username;
    public String[] tags;

    public UserResponse() {
    }

    public UserResponse(String username, String[] tags) {
        this.username = username;
        this.tags = tags;
    }
}
