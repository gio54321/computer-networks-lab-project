package winsome.common.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FollowersCallbackService extends Remote {
    public void registerForCallback(int userId, FollowersCallback callback) throws RemoteException;

    public void unregisterForCallback(int userId) throws RemoteException;
}
