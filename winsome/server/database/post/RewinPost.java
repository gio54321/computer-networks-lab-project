package winsome.server.database.post;

public class RewinPost extends Post {
    private int rewinPostId;

    public RewinPost(String authorUsername, int rewinPostId) {
        super(authorUsername);
        this.rewinPostId = rewinPostId;
    }

    public int getRewinPostId() {
        return rewinPostId;
    }

    public void setRewinPostId(int rewinPostId) {
        this.rewinPostId = rewinPostId;
    }
}
