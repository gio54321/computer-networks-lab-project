package winsome.server.database;

import java.util.HashSet;

public class Post {
    private String title;
    private String content;
    private String authorUsername;
    private HashSet<String> votersUsernames = new HashSet<>();
    private int positiveVotes = 0;
    private int negativeVotes = 0;

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
}
