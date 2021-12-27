package winsome.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ClientMain {
    public static void main(String[] args) {
        try {
            var connection = new WinsomeConnection("localhost");
            String[] tags = { "art", "sport" };
            connection.register("pippo", "password", tags);
        } catch (RemoteException | NotBoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
