package winsome.server.database;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import winsome.common.responses.PartialRewardResponse;
import winsome.server.database.serializables.SerializableUser;

public class User {
    private String username;
    private String password;
    private HashSet<String> tags = new HashSet<>();

    // the set of followers usernames
    private HashSet<String> followers = new HashSet<>();
    // the set of followed usernames
    private HashSet<String> followed = new HashSet<>();
    // the set of rewinned posts ids
    private HashSet<Integer> rewinnedPosts = new HashSet<>();
    // the set of authored posts ids
    private HashSet<Integer> authoredPosts = new HashSet<>();

    // the wallet amount
    private double wallet = 0.0;
    // the reward increment list, consisting of the history of all the rewards
    // earned by the user
    private List<PartialReward> rewardIncrementList = new ArrayList<>();

    public User() {
    }

    public User(String username, String password, String[] tags) {
        if (username == null || password == null || tags == null) {
            throw new NullPointerException();
        }

        this.username = username;
        this.password = password;

        for (var t : tags) {
            this.tags.add(t);
        }
    }

    /**
     * Check if the user has given tag
     * 
     * @param tag
     * @return true if the user has given tag
     */
    public boolean hasTag(String tag) {
        return this.tags.contains(tag);
    }

    /**
     * Check if this has any tags in common with otherUser
     * 
     * @param otherUser
     * @return true if there are tags in common with otherUser
     */
    public boolean hasTagInCommon(User otherUser) {
        if (otherUser == null) {
            throw new NullPointerException();
        }

        for (var t : this.tags) {
            if (otherUser.hasTag(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add user to the follower set
     * 
     * @param username
     */
    public void addFollower(String username) {
        this.followers.add(username);
    }

    /**
     * Remove user from the follower set
     * 
     * @param username
     */
    public void removeFollower(String username) {
        this.followers.remove(username);
    }

    /**
     * Add user to the followed set
     * 
     * @param username
     */
    public void addFollowed(String username) {
        this.followed.add(username);
    }

    /**
     * Remove user from the followed set
     * 
     * @param username
     */
    public void removeFollowed(String username) {
        this.followed.remove(username);
    }

    /**
     * Add post id to the authored set
     * 
     * @param idPost
     * @return true if the post was not already in the set
     */
    public boolean addAuthoredPost(int idPost) {
        return this.authoredPosts.add(idPost);
    }

    /**
     * Remove post id from the authored set
     * 
     * @param idPost
     */
    public void removeAuthoredPost(int idPost) {
        this.authoredPosts.remove(idPost);
    }

    /**
     * Add post id to the rewinned set
     * 
     * @param idPost
     * @return true if the post was not already in the set
     */
    public boolean addRewinnedPost(int idPost) {
        return this.rewinnedPosts.add(idPost);
    }

    /**
     * Remove post id from the rewinned set
     * 
     * @param idPost
     */
    public void removeRewinnedPost(int idPost) {
        this.rewinnedPosts.remove(idPost);
    }

    /**
     * Add partial reward entry to reward history
     * 
     * @param timestamp     the timestamp of the reward
     * @param partialReward the partial amount
     */
    public void addRewardEntry(long timestamp, double partialReward) {
        this.rewardIncrementList.add(new PartialReward(timestamp, partialReward));
        this.wallet += partialReward;
    }

    /**
     * Get the history of partial rewards.
     * 
     * @return the history of the rewards as a list of PartialRewardResponse
     */
    public List<PartialRewardResponse> getPartialRewardResponseList() {
        var outList = new ArrayList<PartialRewardResponse>();
        for (var entry : this.rewardIncrementList) {
            var p = new PartialRewardResponse();
            p.timestamp = entry.getTimestamp();
            p.partialReward = entry.getPartialReward();
            outList.add(p);
        }
        return outList;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String[] getTags() {
        var res = new String[this.tags.size()];
        int i = 0;
        for (String tag : this.tags) {
            res[i] = tag;
            ++i;
        }
        return res;
    }

    public double getWallet() {
        return this.wallet;
    }

    public Set<String> getFollowed() {
        return new HashSet<String>(this.followed);
    }

    public Set<String> getFollowers() {
        return new HashSet<String>(this.followers);
    }

    public Set<Integer> getRewins() {
        return new HashSet<Integer>(this.rewinnedPosts);
    }

    public Set<Integer> getAuthoredPosts() {
        return new HashSet<Integer>(this.authoredPosts);
    }

    /**
     * Clone object to a serializable version of it
     * 
     * @return the cloned serializable object
     */
    public SerializableUser cloneToSerializable() {
        var out = new SerializableUser();

        // clone fields
        out.username = this.username;
        out.password = this.password;
        out.tags = new HashSet<>(this.tags);
        out.followers = new HashSet<>(this.followers);
        out.followed = new HashSet<>(this.followed);
        out.rewinnedPosts = new HashSet<>(this.rewinnedPosts);
        out.authoredPosts = new HashSet<>(this.authoredPosts);

        out.wallet = this.wallet;

        // clone the reward history
        out.rewardIncrementList = new ArrayList<>();
        for (var entry : this.rewardIncrementList) {
            out.rewardIncrementList.add(entry.cloneToSerializable());
        }

        return out;
    }

    /**
     * Clone serializable object into this
     * 
     * @param comment the serializable object
     */
    public void fromSerializable(SerializableUser user) {
        // clone the field
        this.username = user.username;
        this.password = user.password;
        this.tags = new HashSet<>(user.tags);
        this.followers = new HashSet<>(user.followers);
        this.followed = new HashSet<>(user.followed);
        this.rewinnedPosts = new HashSet<>(user.rewinnedPosts);
        this.authoredPosts = new HashSet<>(user.authoredPosts);

        this.wallet = user.wallet;

        // clone the reward history
        this.rewardIncrementList.clear();
        for (var entry : user.rewardIncrementList) {
            var newEntry = new PartialReward(entry.timestamp, entry.partialReward);
            this.rewardIncrementList.add(newEntry);
        }
    }
}
