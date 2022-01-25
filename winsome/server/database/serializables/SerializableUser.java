package winsome.server.database.serializables;

import java.util.Set;

public class SerializableUser {
    public String username;
    public String password;
    public Set<String> tags;
    public Set<String> followers;
    public Set<String> followed;
    public Set<Integer> rewinnedPosts;
    public Set<Integer> authoredPosts;
}
