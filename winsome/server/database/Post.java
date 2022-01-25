package winsome.server.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Post {
    private int postId;
    private String title;
    private String content;
    private String authorUsername;
    private HashSet<String> votersUsernames = new HashSet<>();
    private int positiveVotes = 0;
    private int negativeVotes = 0;
    private List<Comment> comments = new ArrayList<Comment>();

    // rewards statistics
    private long age = 0;
    private int newPositiveVotes = 0;
    private int newNegativeVotes = 0;
    private Map<String, Integer> newCommentsCount = new HashMap<>();

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
            newPositiveVotes++;
        } else {
            negativeVotes++;
            newNegativeVotes++;
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
        this.newCommentsCount.compute(author, (k, v) -> {
            if (v == null) {
                // this is the first comment of this reward iteration
                return 1;
            } else {
                // increment the author's comment count
                return v + 1;
            }
        });
    }

    public List<Comment> getComments() {
        return new ArrayList<>(this.comments);
    }

    public double calculateNewReward() {
        // first increment the age of the post, since the age starts at 0, the first
        // iteration will be age==1
        this.age++;

        // if noop, then return -1
        if (this.newCommentsCount.size() == 0 && this.newPositiveVotes <= this.newNegativeVotes) {
            // reset iteration counters
            this.newNegativeVotes = 0;
            this.newPositiveVotes = 0;
            this.newCommentsCount.clear();
            return -1;
        }

        double votesReward = Math.log(1 + Math.max(0.0, this.newPositiveVotes - this.newNegativeVotes));

        double commentsReward = 0.0;
        for (var cp : this.newCommentsCount.values()) {
            System.out.println("commenti: " + Integer.toString(cp));
            commentsReward += 2.0 / (1.0 + Math.exp(-cp + 1));
        }
        commentsReward = Math.log(commentsReward + 1);

        // reset iteration counters
        this.newNegativeVotes = 0;
        this.newPositiveVotes = 0;
        this.newCommentsCount.clear();

        return (votesReward + commentsReward) / this.age;
    }
}
