package winsome.client;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import winsome.common.responses.UserResponse;
import winsome.common.rmi.FollowersCallback;

public class FollowersCallbackImpl extends RemoteObject implements FollowersCallback {
    private HashMap<String, UserResponse> followers;

    public FollowersCallbackImpl(List<UserResponse> initialFollowers) throws RemoteException {
        super();
        this.followers = new HashMap<>();
        for (var user : initialFollowers) {
            this.followers.put(user.username, user);
        }
    }

    public List<UserResponse> getFollowers() {
        var list = new ArrayList<UserResponse>();
        list.addAll(this.followers.values());
        return list;
    }

    public void notifyFollowed(UserResponse user) throws RemoteException {
        System.out.println("NOTIFICATION: user " + user.username + " started following");
        this.followers.put(user.username, user);
    }

    public void notifyUnfollowed(UserResponse user) throws RemoteException {
        System.out.println("NOTIFICATION: user " + user.username + " stopped following");
        this.followers.remove(user.username);
    }

}
