package winsome.server.database;

import winsome.server.database.serializables.SerializableComment;

/**
 * Class that represent a comment, composed of an author and a content
 */
public class Comment {
    private String author;
    private String content;

    public Comment() {
    }

    public Comment(String author, String content) {
        this.author = author;
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Clone object to a serializable version of it
     * 
     * @return the cloned serializable object
     */
    public SerializableComment cloneToSerializable() {
        var out = new SerializableComment();
        out.author = this.author;
        out.content = this.content;
        return out;
    }

    /**
     * Clone serializable object into this
     * 
     * @param comment the serializable object
     */
    public void fromSerializable(SerializableComment comment) {
        this.author = comment.author;
        this.content = comment.content;
    }
}