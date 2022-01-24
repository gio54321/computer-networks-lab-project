package winsome.common.responses;

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
