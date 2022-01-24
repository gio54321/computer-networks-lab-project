package winsome.server.database.post;

public abstract class Post {
    private String authorUsername;

    public Post(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }
}
