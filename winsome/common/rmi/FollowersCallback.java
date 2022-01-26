package winsome.common.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import winsome.common.responses.UserResponse;

/**
 * Interface for follower callback objects. All objects that are exported by
 * clients
 * to implement the followers callback functionality must extend this interface
 */
public interface FollowersCallback extends Remote {
    /**
     * Notify the client that user has started following
     * 
     * @param user the user that started following
     * @throws RemoteException
     */
    public void notifyFollowed(UserResponse user) throws RemoteException;

    /**
     * Notify the client that user has stopped following
     * 
     * @param user the user that stopped following
     * @throws RemoteException
     */
    public void notifyUnfollowed(UserResponse user) throws RemoteException;
}
