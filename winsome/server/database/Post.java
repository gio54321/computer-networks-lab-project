package winsome.server.database;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Post {
    private int postId;
    private String title;
    private String content;
    private String authorUsername;
    private HashSet<String> votersUsernames = new HashSet<>();
    private int positiveVotes = 0;
    private int negativeVotes = 0;
    private List<Comment> comments = new ArrayList<Comment>();

    public Post(int postId, String authorUsername, String title, String content) {
        this.postId = postId;
        this.authorUsername = authorUsername;
        this.title = title;
        this.content = content;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
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

    public boolean hasUserRated(String username) {
        return this.votersUsernames.contains(username);
    }

    public void addRate(String username, int rate) {
        this.votersUsernames.add(username);
        if (rate > 0) {
            positiveVotes++;
        } else {
            negativeVotes++;
        }
    }

    public int getPositiveVotesCount() {
        return this.positiveVotes;
    }

    public int getNegativeVotesCount() {
        return this.negativeVotes;
    }

    public void addComment(String author, String content) {
        var comment = new Comment(author, content);
        this.comments.add(comment);
    }

    public List<Comment> getComments() {
        return new ArrayList<>(this.comments);
    }

}
