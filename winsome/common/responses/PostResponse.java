package winsome.common.responses;

public class PostResponse {
    public int postId;
    public String author;
    public String title;
    public String content;

    public PostResponse(int postId, String author, String title, String content) {
        this.postId = postId;
        this.author = author;
        this.title = title;
        this.content = content;
    }

    public PostResponse() {
    }
}
