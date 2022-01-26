package winsome.server.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import winsome.server.database.serializables.SerializablePost;

public class Post {
    // the post id
    private int postId;
    // the post title
    private String title;
    // the post content
    private String content;
    // the author username
    private String authorUsername;

    // the set of usernames that rated the post
    private HashSet<String> votersUsernames = new HashSet<>();
    // the number of positive votes
    private int positiveVotes = 0;
    // the number of negative votes
    private int negativeVotes = 0;
    // the list of the post's comments
    private List<Comment> comments = new ArrayList<Comment>();

    // ---- rewards statistics

    // age of the post, aka the number of reward calculations that has been done
    // on the post
    private long age = 0;
    // set of usernames that have rated the post +1 since the last reward
    // calculation
    private HashSet<String> newPositiveVotersUsernames = new HashSet<>();
    // number of users that have rated positively the post since the last reward
    // calculation
    private int newPositiveVotes = 0;
    // number of users that have rated negatively the post since the last reward
    // calculation
    private int newNegativeVotes = 0;
    // map of comment count that maps username -> number of comment made on the post
    // since the last reward calculation
    private Map<String, Integer> newCommentsCount = new HashMap<>();

    public Post(int postId, String authorUsername, String title, String content) {
        this.postId = postId;
        this.authorUsername = authorUsername;
        this.title = title;
        this.content = content;
    }

    public Post() {
    }

    /**
     * Check if the user has rated this post
     * 
     * @param username the username to check
     * @return true if username has voted this post
     */
    public boolean hasUserRated(String username) {
        if (username == null) {
            throw new NullPointerException();
        }
        return this.votersUsernames.contains(username);
    }

    /**
     * Add rate to post
     * The username is added to the voters of the post
     * 
     * @param username
     * @param rate
     */
    public void addRate(String username, int rate) {
        if (username == null) {
            throw new NullPointerException();
        }
        this.votersUsernames.add(username);
        if (rate > 0) {
            this.positiveVotes++;
            this.newPositiveVotes++;
            this.newPositiveVotersUsernames.add(username);
        } else {
            this.negativeVotes++;
            this.newNegativeVotes++;
        }
    }

    /**
     * Add comment to post
     * 
     * @param author  the author of the comment
     * @param content the content of the comment
     */
    public void addComment(String author, String content) {
        if (author == null || content == null) {
            throw new NullPointerException();
        }
        var comment = new Comment(author, content);
        this.comments.add(comment);

        // increment the counter in newCommentsCount
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

    /**
     * Get curators usernames, that is the set of usernames that have commented or
     * rated positive the post since the last reward calculation
     * 
     * @return the set of curators' usernames
     */
    public Set<String> getCuratorsUsernames() {
        var out = new HashSet<String>();
        // add the new positive vote users
        out.addAll(this.newPositiveVotersUsernames);
        // add the new comments authors
        out.addAll(this.newCommentsCount.keySet());
        return out;
    }

    /**
     * Calculate the reward of the post
     * This method will return the total reward, that has to be splitted between
     * author and curators.
     * This methods increment the age of the post and resets the internal state for
     * the new positive votes and new comments.
     * 
     * @return a negative number if no reward has to be assigned, a positive number
     *         corresponding to the calculated reward otherwise
     */
    public double calculateNewReward() {
        // first increment the age of the post, since the age starts at 0, the first
        // iteration will be age==1
        this.age++;

        // if no operation has to be done, then reset counters and return -1
        if (this.newCommentsCount.size() == 0 && this.newPositiveVotes <= this.newNegativeVotes) {
            // reset iteration counters
            this.newNegativeVotes = 0;
            this.newPositiveVotes = 0;
            this.newCommentsCount.clear();
            return -1.0;
        }

        // calculate the votes reward
        double votesReward = Math.log(1 + Math.max(0.0, this.newPositiveVotes - this.newNegativeVotes));

        // caluclate the comments reward
        double commentsReward = 0.0;
        for (var cp : this.newCommentsCount.values()) {
            commentsReward += 2.0 / (1.0 + Math.exp(-cp + 1));
        }
        commentsReward = Math.log(commentsReward + 1);

        // reset iteration counters and sets
        this.newNegativeVotes = 0;
        this.newPositiveVotes = 0;
        this.newCommentsCount.clear();
        this.newPositiveVotersUsernames.clear();

        // return the actual reward
        return (votesReward + commentsReward) / this.age;
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

    public int getPositiveVotesCount() {
        return this.positiveVotes;
    }

    public int getNegativeVotesCount() {
        return this.negativeVotes;
    }

    public List<Comment> getComments() {
        return new ArrayList<>(this.comments);
    }

    /**
     * Clone object to a serializable version of it
     * 
     * @return the cloned serializable object
     */
    public SerializablePost cloneToSerializable() {
        var out = new SerializablePost();

        out.postId = this.postId;
        out.title = this.title;
        out.content = this.content;
        out.authorUsername = this.authorUsername;
        out.votersUsernames = new HashSet<>(this.votersUsernames);
        out.positiveVotes = this.positiveVotes;
        out.negativeVotes = this.negativeVotes;
        out.comments = new ArrayList<>();

        for (var c : this.comments) {
            out.comments.add(c.cloneToSerializable());
        }

        // rewards statistics
        out.age = this.age;
        out.newPositiveVotersUsernames = new HashSet<>(this.newPositiveVotersUsernames);
        out.newPositiveVotes = this.newPositiveVotes;
        out.newNegativeVotes = this.newNegativeVotes;
        out.newCommentsCount = new HashMap<>(this.newCommentsCount);
        return out;
    }

    /**
     * Clone serializable object into this
     * 
     * @param comment the serializable object
     */
    public void fromSerializable(SerializablePost post) {
        this.postId = post.postId;
        this.title = post.title;
        this.content = post.content;
        this.authorUsername = post.authorUsername;
        this.votersUsernames = new HashSet<>(post.votersUsernames);
        this.positiveVotes = post.positiveVotes;
        this.negativeVotes = post.negativeVotes;
        this.comments = new ArrayList<>();

        for (var c : post.comments) {
            var newComment = new Comment();
            newComment.fromSerializable(c);
            this.comments.add(newComment);
        }

        // rewards statistics
        this.age = post.age;
        this.newPositiveVotersUsernames = new HashSet<>(post.newPositiveVotersUsernames);
        this.newPositiveVotes = post.newPositiveVotes;
        this.newNegativeVotes = post.newNegativeVotes;
        this.newCommentsCount = new HashMap<>(post.newCommentsCount);
    }
}
