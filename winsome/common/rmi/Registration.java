package winsome.common.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Registration extends Remote {
    int registerToWinsome(String username, String password, String[] tags) throws RemoteException;
}
