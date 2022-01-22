package winsome.server.database;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import winsome.common.responses.UserResponse;
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

        if (userAlreadyExists.getValue()) {
            throw new UserAlreadyExistsException();
        }
    }

    public String login(String username, String password)
            throws UserDoesNotExistsException, UserAlreadyLoggedInException, AuthenticationException {
        // thread safe because users does not get removed nor modified
        var user = this.users.get(username);
        if (user == null) {
            throw new UserDoesNotExistsException();
        }

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

    public void logout(String username) {
        this.authTokens.remove(username);
    }

    public boolean authenticateUser(String username, String authToken) {
        Wrapper<Boolean> validAuth = new Wrapper<>(true);
        this.authTokens.compute(username, (k, v) -> {
            if (v == null || !v.contentEquals(authToken)) {
                validAuth.setValue(false);
            }
            return v;
        });
        return validAuth.getValue();
    }

    // TODO documentation
    public List<UserResponse> listUsers(String username) {
        var list = new ArrayList<UserResponse>();
        var callingUser = this.users.get(username);
        this.users.forEach((k, v) -> {
            if (!k.contentEquals(username) && callingUser.hasTagInCommon(v)) {
                var r = new UserResponse();
                r.username = k;
                r.tags = v.getTags();
                list.add(r);
            }
        });
        return list;
    }

    public void followUser(String username, String toFollowUser) {

    }
}
