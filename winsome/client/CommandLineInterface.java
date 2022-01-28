package winsome.client;

import java.io.IOException;
import java.util.Scanner;

import winsome.lib.utils.Result;

/**
 * Class that implements the command line interface.
 * In particular the runInterpreter() function executes an infinte loop of
 * read-evalutation-print result until the user digits the "exit" command
 */
public class CommandLineInterface {
    private WinsomeConnection connection;

    public CommandLineInterface(WinsomeConnection connection) {
        if (connection == null) {
            throw new NullPointerException();
        }
        this.connection = connection;
    }

    public void runInterpreter() throws IOException {
        // create a new scanner on the stdin
        var inputScanner = new Scanner(System.in);

        while (true) {
            // get the next command
            var line = inputScanner.nextLine();

            // if the command is exactly "exit" then close the connection and exit from the
            // infinite loop
            if (line.contentEquals("exit")) {
                this.connection.closeConnection();
                break;
            }

            // split the command into tokens
            var tokens = line.split(" ");

            // if there isn't at least one token then print an error
            if (tokens.length < 1) {
                System.out.println("Error: invalid command");
                continue;
            }

            // begin parsing all the different commands
            if (tokens[0].contentEquals("help")) {
                // print help command
                var helpStr = "Manuale dei comandi\n\n";
                helpStr += "register <username> <password> [lista di tag]: esegui la registrazione; "
                        + "la lista di tag è una lista di token separati da uno spazio\n";
                helpStr += "login <username> <password>: esegui il login\n";
                helpStr += "logout: esegui il logout\n";
                helpStr += "follow <username>: inizia a seguire l'utente <username>\n";
                helpStr += "unfollow <username>: smetti di seguire l'utente <username>\n";
                helpStr += "list users: ottieni la lista degli utenti che hanno almeno un tag in comune\n";
                helpStr += "list following: ottieni la lista degli utenti seguiti\n";
                helpStr += "list followers: ottieni la lista degli utenti che ti seguono\n";
                helpStr += "post <titolo> <contenuto>: crea un post; titolo e contenuto sono stringhe racchiuse da virgolette";
                helpStr += "rewin <postId>: effettua il rewin di un post\n";
                helpStr += "rate <postId> [+1|-1]: aggiungi un voto a un post; il voto deve essere necessariamente '+1' o '-1'\n";
                helpStr += "comment <postId> <contenuto>: aggiungi un commento a un post; il contenuto è una stringa raggiusa da virgolette\n";
                helpStr += "blog: visualizza il tuo blog\n";
                helpStr += "feed: visualizza il tuo feed\n";
                helpStr += "delete <postId>: rimuovi un post\n";
                helpStr += "wallet: visualizza il tuo wallet\n";
                helpStr += "wallet btc: visualizza il tuo wallet convertito in BTC\n";
                helpStr += "exit: termina il client\n";
                System.out.println(helpStr);

            } else if (tokens[0].contentEquals("register")) {
                if (tokens.length < 3 || tokens.length > 8) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }

                // copy the tags arguments to the tags array
                var tags = new String[tokens.length - 3];
                for (int i = 3; i < tokens.length; ++i) {
                    tags[i - 3] = tokens[i];
                }

                // invoke the registration method and print the result
                var res = this.connection.register(tokens[1], tokens[2], tags);
                printResult(res);

            } else if (tokens[0].contentEquals("login")) {
                if (tokens.length != 3) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                // invoke the login method and print the result
                printResult(this.connection.login(tokens[1], tokens[2]));

            } else if (tokens[0].contentEquals("logout")) {
                if (tokens.length != 1) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                // invoke the logout method and print the result
                printResult(this.connection.logout());

            } else if (tokens[0].contentEquals("follow")) {
                if (tokens.length != 2) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                // invoke the follow method and print the result
                printResult(this.connection.followUser(tokens[1]));

            } else if (tokens[0].contentEquals("unfollow")) {
                if (tokens.length != 2) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                // invoke the unfollow method and print the result
                printResult(this.connection.unfollowUser(tokens[1]));

            } else if (tokens.length >= 2
                    && tokens[0].contentEquals("list") && tokens[1].contentEquals("users")) {
                if (tokens.length != 2) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                // invoke the list users method and print the result
                printResult(this.connection.listUsers());

            } else if (tokens.length >= 2
                    && tokens[0].contentEquals("list") && tokens[1].contentEquals("following")) {
                if (tokens.length != 2) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                // invoke the list following method and print the result
                printResult(this.connection.listFollowing());

            } else if (tokens.length >= 2
                    && tokens[0].contentEquals("list") && tokens[1].contentEquals("followers")) {
                if (tokens.length != 2) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                // invoke the list followers method and print the result
                printResult(this.connection.listFollowers());

            } else if (tokens[0].contentEquals("post")) {
                // since the post command has a different syntax, it requires special parsing
                // the post syntax can contain quotes
                // for example: post "My title" "My content"

                // take the arguments to the post command, in particular arguments[1] is the
                // substring of the line after the first space
                var arguments = line.split(" ", 2);
                if (arguments.length != 2) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }

                // ten split the arguments by quotes
                var contents = arguments[1].split("\"");
                if (contents.length != 4) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }

                // get the title and content substrings
                var title = contents[1];
                var content = contents[3];

                // invoke the create post method and print the result
                printResult(this.connection.createPost(title, content));

            } else if (tokens.length >= 2
                    && tokens[0].contentEquals("show") && tokens[1].contentEquals("post")) {
                if (tokens.length != 3) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                try {
                    // try to parse an integer from the second argument
                    var postId = Integer.parseInt(tokens[2]);
                    // invoke the get post method and print the result
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
                    // try to parse an integer from the second argument
                    var postId = Integer.parseInt(tokens[1]);
                    // invoke the rewin post method and print the result
                    printResult(this.connection.rewinPost(postId));
                } catch (NumberFormatException e) {
                    System.out.println("Error: invalid post id: " + tokens[2]);
                }

            } else if (tokens[0].contentEquals("rate")) {
                if (tokens.length != 3) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }

                // check that the vote is either "+1" or "-1"
                if (!tokens[2].contentEquals("+1") && !tokens[2].contentEquals("-1")) {
                    System.out.println("Error: vote must be +1 or -1");
                    continue;
                }
                // actually parse the vote
                var vote = 1;
                if (tokens[2].contentEquals("-1")) {
                    vote = -1;
                }

                try {
                    // try to parse an integer from the second argument
                    var postId = Integer.parseInt(tokens[1]);
                    // invoke the rate post method and print the result
                    printResult(this.connection.ratePost(postId, vote));
                } catch (NumberFormatException e) {
                    System.out.println("Error: invalid post id: " + tokens[1]);
                }

            } else if (tokens[0].contentEquals("comment")) {
                // linke for the post command, the comment command can have quotes
                // separating the content of the comment
                // example: comment <postId> "my comment content"

                // get the arguments
                var arguments = line.split(" ", 3);
                if (arguments.length != 3) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }

                // get the quoted content of the comment
                var contents = arguments[2].split("\"");
                if (contents.length != 2) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                var content = contents[1];

                try {
                    // try to parse an integer from the second argument
                    var postId = Integer.parseInt(arguments[1]);
                    // invoke the add comment method and print the result
                    printResult(this.connection.addComment(postId, content));
                } catch (NumberFormatException e) {
                    System.out.println("Error: invalid post id: " + tokens[1]);
                }

            } else if (tokens[0].contentEquals("blog")) {
                if (tokens.length != 1) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                // invoke the view blog method and print the result
                printResult(this.connection.viewBlog());

            } else if (tokens[0].contentEquals("feed")) {
                if (tokens.length != 1) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                // invoke the view feed method and print the result
                printResult(this.connection.viewFeed());

            } else if (tokens[0].contentEquals("delete")) {
                if (tokens.length != 2) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                try {
                    // try to parse an integer from the second argument
                    var postId = Integer.parseInt(tokens[1]);
                    // invoke the delete post method and print the result
                    printResult(this.connection.deletePost(postId));
                } catch (NumberFormatException e) {
                    System.out.println("Error: invalid post id: " + tokens[2]);
                }

            } else if (tokens.length >= 2 && tokens[0].contentEquals("wallet") && tokens[1].contentEquals("btc")) {
                if (tokens.length != 2) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                // invoke the get wallet in btc method and print the result
                printResult(this.connection.getWalletInBtc());

            } else if (tokens[0].contentEquals("wallet")) {
                if (tokens.length != 1) {
                    System.out.println("Error: invalid arguments");
                    continue;
                }
                // invoke the get wallet method and print the result
                printResult(this.connection.getWallet());
            } else {
                // if none of the ifs guards verified, then print an error
                // for command not found
                System.out.println("Error: command not found");
            }
        }
        // close the scanner
        inputScanner.close();
    }

    /**
     * Helper function that prints a result
     * In particular it prints Err results by prepending "Error: "
     * 
     * @param res the result to be printed
     */
    private void printResult(Result<String, String> res) {
        if (res == null) {
            throw new NullPointerException();
        }
        if (res.isOk()) {
            System.out.println(res.getOkValue());
        } else {
            System.out.println("Error: " + res.getErrValue());
        }
    }

}
