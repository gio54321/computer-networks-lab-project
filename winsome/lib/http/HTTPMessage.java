package winsome.lib.http;

public abstract class HTTPMessage {
    public abstract String getFormattedMessage();

    public abstract void parseFormattedMessage(String message) throws HTTPParsingException;

}
