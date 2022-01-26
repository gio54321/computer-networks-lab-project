package winsome.client;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import winsome.common.responses.UserResponse;
import winsome.common.rmi.FollowersCallback;

/**
 * Implementation of the FllowersCallback interface.
 * Used by the client to hold the followers set and for receiving notifications
 * by the server for updates of the followers set
 */
public class FollowersCallbackImpl extends RemoteObject implements FollowersCallback {
    private HashMap<String, UserResponse> followers;

    /**
     * Create a new FollowersCallbackImpl with the given initial followers.
     * 
     * @param initialFollowers the list of the initial followers
     * @throws RemoteException
     */
    public FollowersCallbackImpl(List<UserResponse> initialFollowers) throws RemoteException {
        super();
        if (initialFollowers == null) {
            throw new NullPointerException();
        }
        this.followers = new HashMap<>();
        for (var user : initialFollowers) {
            this.followers.put(user.username, user);
        }
    }

    /**
     * Get the current list of all the followers
     * 
     * @return the list of followers
     */
    public List<UserResponse> getFollowers() {
        var list = new ArrayList<UserResponse>();
        list.addAll(this.followers.values());
        return list;
    }

    /**
     * Method used by the server to notify that a new user has started following
     * This methods puts the new user in the followers list
     */
    public void notifyFollowed(UserResponse user) throws RemoteException {
        if (user == null) {
            throw new NullPointerException();
        }
        System.out.println("NOTIFICATION: user " + user.username + " started following");
        this.followers.put(user.username, user);
    }

    /**
     * Method used by the server to notify that a new user has stopped following
     * This methods removes the user from the followers list
     */
    public void notifyUnfollowed(UserResponse user) throws RemoteException {
        if (user == null) {
            throw new NullPointerException();
        }
        System.out.println("NOTIFICATION: user " + user.username + " stopped following");
        this.followers.remove(user.username);
    }

}
