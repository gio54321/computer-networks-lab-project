package winsome.server.database.serializables;

import java.util.Map;

public class SerializableDatabase {
    public int initPostId;
    public Map<String, SerializableUser> users;
    public Map<Integer, SerializablePost> posts;
}
