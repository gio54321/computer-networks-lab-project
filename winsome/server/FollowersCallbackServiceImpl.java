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

/**
 * Server implementation of the followers callback service
 */
public class FollowersCallbackServiceImpl extends RemoteServer implements FollowersCallbackService {
    // the map of callbacks
    private HashMap<String, FollowersCallback> callbacks = new HashMap<>();
    // the auth interface used to authenticate users
    private AuthenticationInterface authInterface;
    // reference to the server's database
    private Database database;

    public FollowersCallbackServiceImpl(AuthenticationInterface authInterface, Database database) {
        super();
        this.authInterface = authInterface;
        this.database = database;
    }

    /**
     * Register the callback to the server. The user must provide a valid
     * pair username authToken
     * 
     * @param username  the calling username
     * @param authToken the calling username's authToken
     * @param callback  the callback to be registered
     * @throws RemoteException
     * @throws AuthenticationException if the username and/or authToken are not
     *                                 valid
     */
    public synchronized void registerForCallback(String username, String authToken, FollowersCallback callback)
            throws RemoteException, AuthenticationException {
        if (!this.authInterface.authenticateUser(username, authToken)) {
            throw new AuthenticationException();
        }
        System.out.println("register for callback user " + username + " callback " + callback);
        this.callbacks.put(username, callback);
    }

    /**
     * Unregister the callback to the server. The user must provide a valid
     * pair username authToken
     * 
     * @param username  the calling username
     * @param authToken the calling username's authToken
     * @throws RemoteException
     * @throws AuthenticationException if the username and/or authToken are not
     *                                 valid
     */
    public synchronized void unregisterForCallback(String username, String authToken)
            throws RemoteException, AuthenticationException {
        if (!this.authInterface.authenticateUser(username, authToken)) {
            throw new AuthenticationException();
        }
        this.callbacks.remove(username);
    }

    /**
     * Get the current followers list.
     * The user must provide a valid pair username authToken
     * 
     * @param username  the calling username
     * @param authToken the calling username's authToken
     * @return the list of followers
     * @throws RemoteException
     * @throws AuthenticationException if the username and/or authToken are not
     *                                 valid
     */
    public synchronized List<UserResponse> getFollowers(String username, String authToken)
            throws RemoteException, AuthenticationException {
        if (!this.authInterface.authenticateUser(username, authToken)) {
            throw new AuthenticationException();
        }
        return this.database.listFollowers(username);
    }

    /**
     * Notify new follower to user
     * 
     * @param username     the username that has to be notified
     * @param followedUser the new follower
     * @throws RemoteException
     */
    public synchronized void notifyFollow(String username, UserResponse followedUser) throws RemoteException {
        System.out.println("notify followed " + username + " -> " + followedUser.username);
        if (this.callbacks.containsKey(username)) {
            this.callbacks.get(username).notifyFollowed(followedUser);
        }
    }

    /**
     * Notify unfollowing action to user
     * 
     * @param username     the username that has to be notified
     * @param followedUser the unfollower
     * @throws RemoteException
     */
    public synchronized void notifyUnfollow(String username, UserResponse unfollowedUser) throws RemoteException {
        System.out.println("notify unfollowed " + username + " -> " + unfollowedUser.username);
        if (this.callbacks.containsKey(username)) {
            this.callbacks.get(username).notifyUnfollowed(unfollowedUser);
        }
    }
}
