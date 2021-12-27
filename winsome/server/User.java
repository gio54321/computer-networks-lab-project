package winsome.server;

public class User {
    private String username;
    private String password;
    private String[] tags;

    public User(String username, String password, String[] tags) {
        if (username == null || password == null || tags == null) {
            throw new NullPointerException();
        }

        this.username = username;
        this.password = password;
        this.tags = tags;
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

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

}
