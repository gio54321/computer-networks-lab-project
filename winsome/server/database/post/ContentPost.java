package winsome.server.database.post;

public class ContentPost extends Post {
    private String title;
    private String content;

    public ContentPost(String authorUsername, String title, String content) {
        super(authorUsername);
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
