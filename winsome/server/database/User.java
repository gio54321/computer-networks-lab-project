package winsome.server.database;

import java.util.HashSet;
import java.util.Set;

public class User {
    private String username;
    private String password;
    private HashSet<String> tags = new HashSet<>();
    private HashSet<String> followers = new HashSet<>();
    private HashSet<String> followed = new HashSet<>();

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
}
