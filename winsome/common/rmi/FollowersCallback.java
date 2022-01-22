package winsome.common.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FollowersCallback extends Remote {
    public void notifyFollowed(String username) throws RemoteException;

    public void notifyUnfollowed(String username) throws RemoteException;
}
