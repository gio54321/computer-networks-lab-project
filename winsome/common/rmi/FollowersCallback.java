package winsome.common.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import winsome.common.responses.UserResponse;

public interface FollowersCallback extends Remote {
    public void notifyFollowed(UserResponse user) throws RemoteException;

    public void notifyUnfollowed(UserResponse user) throws RemoteException;
}
