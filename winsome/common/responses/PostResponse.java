package winsome.common.responses;

public class PostResponse {
    public int postId;
    public String author;
    public String title;
    public String content;
    public int positiveVoteCount;
    public int negativeVoteCount;

    public PostResponse(int postId, String author, String title, String content, int positiveVoteCount,
            int negativeVoteCount) {
        this.postId = postId;
        this.author = author;
        this.title = title;
        this.content = content;
        this.positiveVoteCount = positiveVoteCount;
        this.negativeVoteCount = negativeVoteCount;
    }

    public PostResponse() {
    }
}
