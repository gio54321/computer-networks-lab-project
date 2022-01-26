package winsome.common.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import winsome.common.responses.UserResponse;
import winsome.server.database.exceptions.AuthenticationException;

/**
 * Inteface for the followers callback service. The server exports one object
 * that implements this interface to enable the registering and unregistering of
 * the callbacks
 */
public interface FollowersCallbackService extends Remote {
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
        public void registerForCallback(String username, String authToken, FollowersCallback callback)
                        throws RemoteException, AuthenticationException;

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
        public void unregisterForCallback(String username, String authToken)
                        throws RemoteException, AuthenticationException;

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
        public List<UserResponse> getFollowers(String username, String authToken)
                        throws RemoteException, AuthenticationException;
}
