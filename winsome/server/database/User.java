package winsome.server.database;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import winsome.common.responses.PartialRewardResponse;
import winsome.lib.utils.Pair;
import winsome.server.database.serializables.SerializableUser;

public class User {
    private String username;
    private String password;
    private HashSet<String> tags = new HashSet<>();
    private HashSet<String> followers = new HashSet<>();
    private HashSet<String> followed = new HashSet<>();
    private HashSet<Integer> rewinnedPosts = new HashSet<>();
    private HashSet<Integer> authoredPosts = new HashSet<>();

    private List<Pair<Long, Double>> rewardIncrementList = new ArrayList<>();
    private double wallet = 0.0;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean hasTag(String tag) {
        return this.tags.contains(tag);
    }

    public boolean hasTagInCommon(User otherUser) {
        for (var t : this.tags) {
            if (otherUser.hasTag(t)) {
                return true;
            }
        }
        return false;
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

    public Set<String> getFollowers() {
        return new HashSet<String>(this.followers);
    }

    public void addFollower(String username) {
        this.followers.add(username);
    }

    public void removeFollower(String username) {
        this.followers.remove(username);
    }

    public Set<String> getFollowed() {
        return new HashSet<String>(this.followed);
    }

    public void addFollowed(String username) {
        this.followed.add(username);
    }

    public void removeFollowed(String username) {
        this.followed.remove(username);
    }

    // return false if the user has already rewinned the post
    public boolean addRewinnedPost(int idPost) {
        return this.rewinnedPosts.add(idPost);
    }

    public boolean addAuthoredPost(int idPost) {
        return this.authoredPosts.add(idPost);
    }

    public void removeAuthoredPost(int idPost) {
        this.authoredPosts.remove(idPost);
    }

    public Set<Integer> getRewins() {
        return new HashSet<Integer>(this.rewinnedPosts);
    }

    public void removeRewinnedPost(int idPost) {
        this.rewinnedPosts.remove(idPost);
    }

    public Set<Integer> getAuthoredPosts() {
        return new HashSet<Integer>(this.authoredPosts);
    }

    public void addRewardEntry(long timestamp, double partialReward) {
        this.rewardIncrementList.add(new Pair<>(timestamp, partialReward));
        this.wallet += partialReward;
    }

    public double getWallet() {
        return this.wallet;
    }

    public List<PartialRewardResponse> getPartialRewardResponseList() {
        var outList = new ArrayList<PartialRewardResponse>();
        for (var entry : this.rewardIncrementList) {
            var p = new PartialRewardResponse();
            p.timestamp = entry.first();
            p.partialReward = entry.second();
            outList.add(p);
        }
        return outList;
    }

    public SerializableUser cloneToSerializable() {
        var out = new SerializableUser();

        out.username = this.username;
        out.password = this.password;
        out.tags = new HashSet<>(this.tags);
        out.followers = new HashSet<>(this.followers);
        out.followed = new HashSet<>(this.followed);
        out.rewinnedPosts = new HashSet<>(this.rewinnedPosts);
        out.authoredPosts = new HashSet<>(this.authoredPosts);

        return out;
    }

    public void fromSerializable(SerializableUser user) {
        this.username = user.username;
        this.password = user.password;
        this.tags = new HashSet<>(user.tags);
        this.followers = new HashSet<>(user.followers);
        this.followed = new HashSet<>(user.followed);
        this.rewinnedPosts = new HashSet<>(user.rewinnedPosts);
        this.authoredPosts = new HashSet<>(user.authoredPosts);
    }
}
