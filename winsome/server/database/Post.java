package winsome.server.database;

public class Post {
    private String title;
    private String content;
    private String authorUsername;

    public Post(String authorUsername, String title, String content) {
        this.authorUsername = authorUsername;
        this.title = title;
        this.content = content;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
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
