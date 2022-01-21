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