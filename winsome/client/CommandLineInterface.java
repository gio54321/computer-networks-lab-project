package winsome.client;

import java.io.IOException;
import java.util.Scanner;

import winsome.lib.utils.Result;

public class CommandLineInterface {
    private WinsomeConnection connection;

    public CommandLineInterface(WinsomeConnection connection) {
        this.connection = connection;
    }

    public void runInterpreter() throws IOException {
        var sc = new Scanner(System.in);

        while (true) {
            var line = sc.nextLine();

            if (line.contentEquals("exit")) {
                break;
            }

            var tokens = line.split(" ");
            if (tokens[0].contentEquals("register")) {
                if (tokens.length < 3 || tokens.length > 8) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }

                // copy the tags to the tags array
                // TODO probably there is a better way?
                var tags = new String[tokens.length - 3];
                for (int i = 3; i < tokens.length; ++i) {
                    tags[i - 3] = tokens[i];
                    System.out.println(tags[i - 3]);
                }
                var res = this.connection.register(tokens[1], tokens[2], tags);
                printResult(res);
            } else if (tokens[0].contentEquals("login")) {
                if (tokens.length != 3) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                var res = this.connection.login(tokens[1], tokens[2]);
                printResult(res);
            } else if (tokens[0].contentEquals("logout")) {
                if (tokens.length != 1) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                printResult(this.connection.logout());
            } else if (tokens[0].contentEquals("follow")) {
                if (tokens.length != 2) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                printResult(this.connection.followUser(tokens[1]));
            } else if (tokens[0].contentEquals("unfollow")) {
                if (tokens.length != 2) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                printResult(this.connection.unfollowUser(tokens[1]));
            } else if (tokens[0].contentEquals("list") && tokens[1].contentEquals("users")) {
                if (tokens.length != 2) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                printResult(this.connection.listUsers());
            } else if (tokens[0].contentEquals("list") && tokens[1].contentEquals("following")) {
                if (tokens.length != 2) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                printResult(this.connection.listFollowing());
            } else if (tokens[0].contentEquals("list") && tokens[1].contentEquals("followers")) {
                if (tokens.length != 2) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                printResult(this.connection.listFollowers());
            } else if (tokens[0].contentEquals("post")) {
                // this requires special parsing
                var arguments = line.split(" ", 2);
                if (arguments.length != 2) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                var contents = arguments[1].split("\"");
                if (contents.length != 4) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                var title = contents[1];
                var content = contents[3];
                printResult(this.connection.createPost(title, content));
            } else if (tokens[0].contentEquals("show") && tokens[1].contentEquals("post")) {
                if (tokens.length != 3) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                try {
                    var postId = Integer.parseInt(tokens[2]);
                    printResult(this.connection.getPost(postId));
                } catch (NumberFormatException e) {
                    System.out.println("Error: invalid post id: " + tokens[2]);
                }
            } else if (tokens[0].contentEquals("rewin")) {
                if (tokens.length != 2) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                try {
                    var postId = Integer.parseInt(tokens[1]);
                    printResult(this.connection.rewinPost(postId));
                } catch (NumberFormatException e) {
                    System.out.println("Error: invalid post id: " + tokens[2]);
                }
            } else if (tokens[0].contentEquals("rate")) {
                if (tokens.length != 3) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                if (!tokens[2].contentEquals("+1") && !tokens[2].contentEquals("-1")) {
                    System.out.println("Error: vote must be +1 or -1");
                    continue;
                }
                // parse the vote
                var vote = 1;
                if (tokens[2].contentEquals("-1")) {
                    vote = -1;
                }

                try {
                    var postId = Integer.parseInt(tokens[1]);
                    printResult(this.connection.ratePost(postId, vote));
                } catch (NumberFormatException e) {
                    System.out.println("Error: invalid post id: " + tokens[1]);
                }
            } else if (tokens[0].contentEquals("comment")) {
                // this requires special parsing
                var arguments = line.split(" ", 3);
                if (arguments.length != 3) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                var contents = arguments[2].split("\"");
                if (contents.length != 2) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                var content = contents[1];
                try {
                    var postId = Integer.parseInt(arguments[1]);
                    printResult(this.connection.addComment(postId, content));
                } catch (NumberFormatException e) {
                    System.out.println("Error: invalid post id: " + tokens[1]);
                }
            } else if (tokens[0].contentEquals("blog")) {
                if (tokens.length != 1) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                printResult(this.connection.viewBlog());
            } else if (tokens[0].contentEquals("feed")) {
                if (tokens.length != 1) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                printResult(this.connection.viewFeed());
            } else if (tokens[0].contentEquals("delete")) {
                if (tokens.length != 2) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                try {
                    var postId = Integer.parseInt(tokens[1]);
                    printResult(this.connection.deletePost(postId));
                } catch (NumberFormatException e) {
                    System.out.println("Error: invalid post id: " + tokens[2]);
                }
            } else if (tokens[0].contentEquals("wallet")) {
                if (tokens.length != 1) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                printResult(this.connection.getWallet());
            } else {
                System.out.println("Error: command not found");
            }
        }
        sc.close();
    }

    private void printResult(Result<String, String> res) {
        if (res.isOk()) {
            System.out.println(res.getOkValue());
        } else {
            System.out.println("Error: " + res.getErrValue());
        }
    }

}
