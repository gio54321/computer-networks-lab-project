package winsome.server.database.serializables;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SerializablePost {
    public int postId;
    public String title;
    public String content;
    public String authorUsername;
    public Set<String> votersUsernames;
    public int positiveVotes = 0;
    public int negativeVotes = 0;
    public List<SerializableComment> comments;

    // rewards statistics
    public long age = 0;
    public int newPositiveVotes = 0;
    public int newNegativeVotes = 0;
    public Map<String, Integer> newCommentsCount;
}
