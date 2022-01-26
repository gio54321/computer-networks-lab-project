package winsome.common.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import winsome.server.database.exceptions.UserAlreadyExistsException;

/**
 * Inteface for the registration service. This provides the registration to
 * Winsome method
 */
public interface Registration extends Remote {
    /**
     * Register an user to Winsome
     * 
     * @param username the username
     * @param password the password
     * @param tags     the list of tags. the length must be less or equal to 5
     * @throws RemoteException
     * @throws UserAlreadyExistsException
     */
    void registerToWinsome(String username, String password, String[] tags)
            throws RemoteException, UserAlreadyExistsException;
}
