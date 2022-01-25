package winsome.server.database;

import winsome.server.database.serializables.SerializableComment;

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

    public SerializableComment cloneToSerializable() {
        var out = new SerializableComment();
        out.author = this.author;
        out.content = this.content;
        return out;
    }

    public void fromSerializable(SerializableComment comment) {
        this.author = comment.author;
        this.content = comment.content;
    }
}