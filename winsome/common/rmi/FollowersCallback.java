package winsome.common.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FollowersCallback extends Remote {
    public void notifyFollowed(int userId) throws RemoteException;

    public void notifyUnfollowed(int userId) throws RemoteException;
}
