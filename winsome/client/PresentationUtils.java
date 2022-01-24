package winsome.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import winsome.common.responses.PostResponse;
import winsome.common.responses.UserResponse;

public class PresentationUtils {

    public static String renderUsernames(UserResponse[] users) {
        return renderUsernames(new ArrayList<>(Arrays.asList(users)));
    }

    public static String renderUsernames(List<UserResponse> users) {
        // username column has to be at least 6 chars wide
        int maxUsernameLength = 6;
        for (var u : users) {
            if (u.username.length() > maxUsernameLength) {
                maxUsernameLength = u.username.length();
            }
        }

        var outStr = "";
        // render the header
        outStr += "Utente";
        for (int i = 6; i <= maxUsernameLength; ++i) {
            outStr += " ";
        }
        outStr += "| Tags\n";

        for (int i = 0; i <= maxUsernameLength + 10; ++i) {
            outStr += "-";
        }
        outStr += "\n";

        for (var u : users) {
            outStr += u.username;
            for (int i = u.username.length(); i <= maxUsernameLength; ++i) {
                outStr += " ";
            }
            outStr += "| ";
            for (var t : u.tags) {
                outStr += t + " ";
            }
            outStr += "\n";
        }
        return outStr;
    }

    public static String renderPost(PostResponse post) {
        var outStr = "";
        outStr += "Author: " + post.author;
        outStr += ", PostId: " + Integer.toString(post.postId) + "\n";
        outStr += "Title: " + post.title + "\n";
        outStr += "Content: " + post.content + "\n";
        outStr += "Voti: positivi " + Integer.toString(post.positiveVoteCount)
                + ", negativi " + Integer.toString(post.negativeVoteCount) + "\n";
        outStr += "Commenti:\n";
        for (var c : post.comments) {
            outStr += "\t" + c.author + ": " + c.content + "\n";
        }

        return outStr;
    }

    public static String renderPostFeed(PostResponse[] posts) {
        // username column has to be at least 7 chars wide
        int maxAuthorLength = 7;
        for (var u : posts) {
            if (u.author.length() > maxAuthorLength) {
                maxAuthorLength = u.author.length();
            }
        }

        var outStr = "";
        // render the header
        outStr += "Id    | Autore ";
        for (int i = 7; i <= maxAuthorLength; ++i) {
            outStr += " ";
        }
        outStr += "| Titolo\n";

        for (int i = 0; i <= maxAuthorLength + 30; ++i) {
            outStr += "-";
        }
        outStr += "\n";

        for (var post : posts) {
            var idStr = Integer.toString(post.postId);
            outStr += idStr;
            for (int i = idStr.length(); i <= 5; ++i) {
                outStr += " ";
            }
            outStr += "| " + post.author;
            // TODO is this correct?
            for (int i = post.author.length(); i <= maxAuthorLength; ++i) {
                outStr += " ";
            }
            outStr += "| " + post.title + "\n";
        }
        return outStr;
    }
}
