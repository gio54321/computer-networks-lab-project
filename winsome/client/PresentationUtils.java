package winsome.client;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import winsome.common.responses.PostResponse;
import winsome.common.responses.UserResponse;
import winsome.common.responses.WalletResponse;

/**
 * Helper class that renders responses data in a nice way
 */
public class PresentationUtils {

    /**
     * Render a list of usernames and their tags
     * 
     * @param users the list of users
     * @return the formatted string
     */
    public static String renderUsernames(UserResponse[] users) {
        return renderUsernames(new ArrayList<>(Arrays.asList(users)));
    }

    /**
     * Render a list of usernames and their tags
     * 
     * @param users the list of users
     * @return the formatted string
     */
    public static String renderUsernames(List<UserResponse> users) {
        if (users == null) {
            throw new NullPointerException();
        }
        // username column has to be at least 4 chars wide
        final int minUsernameLength = 4;

        // calculate the maximum of the uernames lengths
        int maxUsernameLength = minUsernameLength;
        for (var u : users) {
            if (u.username.length() > maxUsernameLength) {
                maxUsernameLength = u.username.length();
            }
        }

        var outStr = "";

        // render the header
        outStr += "User";
        for (int i = minUsernameLength; i <= maxUsernameLength; ++i) {
            outStr += " ";
        }
        outStr += "| Tags\n";

        // render the separator line
        for (int i = 0; i <= maxUsernameLength + 10; ++i) {
            outStr += "-";
        }
        outStr += "\n";

        // render the rows
        for (var u : users) {
            // render the username
            outStr += u.username;
            for (int i = u.username.length(); i <= maxUsernameLength; ++i) {
                outStr += " ";
            }
            outStr += "| ";
            // render the tags
            for (var t : u.tags) {
                outStr += t + " ";
            }
            outStr += "\n";
        }
        return outStr;
    }

    /**
     * Render a post, showing the author, postId, title, content, votes and comments
     * 
     * @param users the list of users
     * @return the formatted string
     */
    public static String renderPost(PostResponse post) {
        if (post == null) {
            throw new NullPointerException();
        }
        var outStr = "";
        outStr += "Author: " + post.author;
        outStr += ", postId: " + Integer.toString(post.postId) + "\n";
        outStr += "Title: " + post.title + "\n";
        outStr += "Content: " + post.content + "\n";
        outStr += "Votes: positives " + Integer.toString(post.positiveVoteCount)
                + ", negatives " + Integer.toString(post.negativeVoteCount) + "\n";
        outStr += "Comments:\n";
        for (var c : post.comments) {
            outStr += "\t" + c.author + ": " + c.content + "\n";
        }

        return outStr;
    }

    public static String renderPostFeed(PostResponse[] posts) {
        if (posts == null) {
            throw new NullPointerException();
        }

        // username column has to be at least 7 chars wide
        final int minAuthorLength = 7;
        // set the id comumn to be 5 chars wide
        final int idColumnwidth = 5;

        // calculate the max author length
        int maxAuthorLength = minAuthorLength;
        for (var u : posts) {
            if (u.author.length() > maxAuthorLength) {
                maxAuthorLength = u.author.length();
            }
        }

        var outStr = "";
        // render the header
        outStr += "Id    | Author ";
        for (int i = minAuthorLength; i <= maxAuthorLength; ++i) {
            outStr += " ";
        }
        outStr += "| Titolo\n";

        // render the separation line
        for (int i = 0; i <= maxAuthorLength + 30; ++i) {
            outStr += "-";
        }
        outStr += "\n";

        // render the posts
        for (var post : posts) {
            // render the id
            var idStr = Integer.toString(post.postId);
            outStr += idStr;
            for (int i = idStr.length(); i <= idColumnwidth; ++i) {
                outStr += " ";
            }

            // render the post author
            outStr += "| " + post.author;
            for (int i = post.author.length(); i <= maxAuthorLength; ++i) {
                outStr += " ";
            }

            // render the post title
            outStr += "| " + post.title + "\n";
        }
        return outStr;
    }

    /**
     * Render a wallet, showing the total amout and the list of partial transactions
     * 
     * @param wallet
     * @return
     */
    public static String renderWallet(WalletResponse wallet) {
        if (wallet == null) {
            throw new NullPointerException();
        }

        // render the wallet total
        var outStr = "Wallet: " + Double.toString(wallet.wallet) + "\n";

        // render the list of partial rewards
        SimpleDateFormat dateFormatter = new SimpleDateFormat("d MMMM yyyy - h:mm:ss");
        for (var entry : wallet.incrementHistory) {
            outStr += dateFormatter.format(entry.timestamp) + ": " + entry.partialReward + "\n";
        }
        return outStr;

    }
}
