package winsome.common.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import winsome.server.database.exceptions.UserAlreadyExistsException;

public interface Registration extends Remote {
    void registerToWinsome(String username, String password, String[] tags)
            throws RemoteException, UserAlreadyExistsException;
}
