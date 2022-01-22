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
                list.add(new UserResponse(k, v.getTags()));
            }
        });
        return list;
    }

    public void followUser(String username, String toFollowUser) throws UserDoesNotExistsException {
        if (!this.users.containsKey(toFollowUser)) {
            throw new UserDoesNotExistsException();
        }

        System.out.println("follow " + username + "->" + toFollowUser);
        this.users.compute(username, (k, v) -> {
            v.addFollowed(toFollowUser);
            return v;
        });
        this.users.compute(toFollowUser, (k, v) -> {
            v.addFollower(username);
            return v;
        });
    }

    public void unfollowUser(String username, String toUnfollowUser) throws UserDoesNotExistsException {
        if (!this.users.containsKey(toUnfollowUser)) {
            throw new UserDoesNotExistsException();
        }
        System.out.println("unfollow " + username + "->" + toUnfollowUser);
        this.users.compute(username, (k, v) -> {
            v.removeFollowed(toUnfollowUser);
            return v;
        });
        this.users.compute(toUnfollowUser, (k, v) -> {
            v.removeFollower(username);
            return v;
        });
    }

    public List<UserResponse> listFollowers(String username) {
        var usernamesList = new ArrayList<String>();
        this.users.compute(username, (k, v) -> {
            usernamesList.addAll(v.getFollowers());
            return v;
        });

        // get the tags of the followers
        var userResponseList = new ArrayList<UserResponse>();
        for (var u : usernamesList) {
            userResponseList.add(new UserResponse(u, this.users.get(u).getTags()));
        }
        return userResponseList;
    }

    public List<UserResponse> listFollowing(String username) {
        var usernamesList = new ArrayList<String>();
        this.users.compute(username, (k, v) -> {
            usernamesList.addAll(v.getFollowed());
            return v;
        });

        // get the tags of the followed
        var userResponseList = new ArrayList<UserResponse>();
        for (var u : usernamesList) {
            userResponseList.add(new UserResponse(u, this.users.get(u).getTags()));
        }
        return userResponseList;
    }
}
