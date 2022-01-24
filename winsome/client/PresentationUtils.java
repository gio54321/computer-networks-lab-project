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
        return outStr;
    }
}
