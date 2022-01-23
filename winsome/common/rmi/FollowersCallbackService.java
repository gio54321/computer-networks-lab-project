package winsome.common.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import winsome.common.responses.UserResponse;
import winsome.server.database.exceptions.AuthenticationException;

public interface FollowersCallbackService extends Remote {
    public void registerForCallback(String username, String authToken, FollowersCallback callback)
            throws RemoteException, AuthenticationException;

    public void unregisterForCallback(String username, String authToken)
            throws RemoteException, AuthenticationException;

    public List<UserResponse> getFollowers(String username, String authToken)
            throws RemoteException, AuthenticationException;
}
