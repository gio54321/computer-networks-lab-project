package winsome.server;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.HashMap;
import java.util.List;

import winsome.common.responses.UserResponse;
import winsome.common.rmi.FollowersCallback;
import winsome.common.rmi.FollowersCallbackService;
import winsome.lib.router.AuthenticationInterface;
import winsome.server.database.Database;
import winsome.server.database.exceptions.AuthenticationException;

public class FollowersCallbackServiceImpl extends RemoteServer implements FollowersCallbackService {
    private HashMap<String, FollowersCallback> callbacks = new HashMap<>();
    private AuthenticationInterface authInterface;
    private Database database;

    public FollowersCallbackServiceImpl(AuthenticationInterface authInterface, Database database) {
        super();
        this.authInterface = authInterface;
        this.database = database;
    }

    public synchronized void registerForCallback(String username, String authToken, FollowersCallback callback)
            throws RemoteException, AuthenticationException {
        if (!this.authInterface.authenticateUser(username, authToken)) {
            throw new AuthenticationException();
        }
        System.out.println("register for callback user " + username + " callback " + callback);
        this.callbacks.put(username, callback);
    }

    public synchronized void unregisterForCallback(String username, String authToken)
            throws RemoteException, AuthenticationException {
        if (!this.authInterface.authenticateUser(username, authToken)) {
            throw new AuthenticationException();
        }
        this.callbacks.remove(username);
    }

    public synchronized List<UserResponse> getFollowers(String username, String authToken)
            throws RemoteException, AuthenticationException {
        if (!this.authInterface.authenticateUser(username, authToken)) {
            throw new AuthenticationException();
        }
        return this.database.listFollowers(username);
    }

    public synchronized void notifyFollow(String username, UserResponse followedUser) throws RemoteException {
        System.out.println("notify followed " + username + " -> " + followedUser.username);
        if (this.callbacks.containsKey(username)) {
            this.callbacks.get(username).notifyFollowed(followedUser);
        }
    }

    public synchronized void notifyUnfollow(String username, UserResponse unfollowedUser) throws RemoteException {
        System.out.println("notify unfollowed " + username + " -> " + unfollowedUser.username);
        if (this.callbacks.containsKey(username)) {
            this.callbacks.get(username).notifyUnfollowed(unfollowedUser);
        }
    }
}
