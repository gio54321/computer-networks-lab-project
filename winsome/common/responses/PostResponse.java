package winsome.common.responses;

/*
 * Response model for a post, containing id, author, title, content,
 * positive and negative vote count and comment list
 * Used for response body serialization
 */
public class PostResponse {
    public int postId;
    public String author;
    public String title;
    public String content;
    public int positiveVoteCount;
    public int negativeVoteCount;
    public CommentResponse[] comments;

    public PostResponse(int postId, String author, String title, String content, int positiveVoteCount,
            int negativeVoteCount, CommentResponse[] comments) {
        this.postId = postId;
        this.author = author;
        this.title = title;
        this.content = content;
        this.positiveVoteCount = positiveVoteCount;
        this.negativeVoteCount = negativeVoteCount;
        this.comments = comments;
    }

    public PostResponse() {
    }
}
