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
    public int positiveVotes;
    public int negativeVotes;
    public List<SerializableComment> comments;

    // rewards statistics
    public long age;
    public Set<String> newPositiveVotersUsernames;
    public int newPositiveVotes;
    public int newNegativeVotes;
    public Map<String, Integer> newCommentsCount;
}
