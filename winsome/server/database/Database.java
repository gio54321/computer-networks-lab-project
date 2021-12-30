package winsome.server.database;

import java.util.concurrent.ConcurrentHashMap;

import winsome.lib.utils.Wrapper;
import winsome.server.database.exceptions.AuthenticationException;
import winsome.server.database.exceptions.UserAlreadyExistsException;
import winsome.server.database.exceptions.UserAlreadyLoggedInException;
import winsome.server.database.exceptions.UserDoesNotExistsException;

public class Database {
    private AuthenticationProvider authProvider = new AuthenticationProvider();
    private ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> authTokens = new ConcurrentHashMap<>();

    public void registerUser(User user) throws UserAlreadyExistsException {
        Wrapper<Boolean> userAlreadyExists = new Wrapper<>(false);
        this.users.compute(user.getUsername(), (k, v) -> {
            if (v == null) {
                // user not already in db
                return user;
            } else {
                userAlreadyExists.setValue(true);
                return v;
            }
        });

        System.out.println("register user");
        if (userAlreadyExists.getValue()) {
            throw new UserAlreadyExistsException();
        }
    }

    public String loginUser(String username, String password)
            throws UserDoesNotExistsException, UserAlreadyLoggedInException, AuthenticationException {
        // thread safe because users does not get removed nor modified
        var user = this.users.get(username);
        if (user == null) {
            throw new UserDoesNotExistsException();
        }

        System.out.println(username);
        System.out.println(password);
        System.out.println(user.getPassword());

        if (!user.getPassword().contentEquals(password)) {
            throw new AuthenticationException();
        }

        Wrapper<Boolean> userAlreadyLoggedIn = new Wrapper<>(false);
        var newAuthToken = this.authProvider.generateNewToken();
        // TODO check for collisions
        this.authTokens.compute(username, (k, v) -> {
            if (v == null) {
                return newAuthToken;
            } else {
                userAlreadyLoggedIn.setValue(true);
                return v;
            }
        });

        if (userAlreadyLoggedIn.getValue()) {
            throw new UserAlreadyLoggedInException();
        }
        return newAuthToken;
    }
}
