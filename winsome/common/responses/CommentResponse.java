package winsome.common.responses;

/*
 * Response model for comment
 * Used for response body serialization
 */
public class CommentResponse extends Response {
    public String author;
    public String content;

    public CommentResponse(String author, String content) {
        this.author = author;
        this.content = content;
    }

    public CommentResponse() {
    }
}
