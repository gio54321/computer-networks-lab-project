package winsome.common.responses;

/*
 * Response model for a post id
 * Used for response body serialization
 */
public class PostIdResponse {
    public int postId;

    public PostIdResponse(int postId) {
        this.postId = postId;
    }

    public PostIdResponse() {
    }
}
