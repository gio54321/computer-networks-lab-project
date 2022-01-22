package winsome.common.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FollowersCallbackService extends Remote {
    public void registerForCallback(String username, FollowersCallback callback) throws RemoteException;

    public void unregisterForCallback(String username) throws RemoteException;
}
