package winsome.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

/**
 * Thread class that impelemnts a notification listener for the rewards
 * notification
 */
public class RewardsNotificationListener extends Thread {
    private MulticastSocket socket;

    /**
     * Create a new notification listener
     * 
     * @param groupAddress the multicast group address
     * @param groupPort    the multicast group port
     * @param netIfName    the network interface name for joining the group
     * @throws IOException
     */
    public RewardsNotificationListener(String groupAddress, int groupPort, String netIfName) throws IOException {
        if (groupAddress == null || netIfName == null) {
            throw new NullPointerException();
        }

        // create a new multicast socket
        this.socket = new MulticastSocket(groupPort);

        // join the group
        var group = new InetSocketAddress(groupAddress, groupPort);

        // get the network interface
        var netIf = NetworkInterface.getByName(netIfName);

        // if the network interface is not valid, then print this error to the user
        // this is not considered a fatal error, since in some implementation, passing
        // null to joinGroup as a network interface still works correctly
        if (netIf == null) {
            System.out.println("ERROR: " + netIfName + " is not a valid network interface name");
        }

        // join the multicast group
        socket.joinGroup(group, netIf);
    }

    /**
     * Run the notification listener.
     * This thread can be correctly interrupted by the interrupt() method
     */
    public void run() {
        // create a new buffer
        var buf = new byte[128];
        while (true) {
            try {
                // try to receive a datagram packet
                var packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                // compare the reulting data with the expected data and print the notification
                // to the user
                var res = new String(packet.getData());
                if (!res.trim().contentEquals("rewards")) {
                    System.out.println("ERROR: rewards notification is not correct");
                } else {
                    System.out.println("NOTIFICATION: rewards updated");
                }
            } catch (IOException e) {
                // this occurrs either because there has been an IOException caused by an
                // anomaly, or because another thread called the interrupt() method
                // in either case, just stop the thread by returning
                return;
            }
        }
    }

    @Override
    public void interrupt() {
        // the termination is done in this way because the receive method does not throw
        // an InterruptedException when interrupted, so when another thread wants to
        // interrupt this thread, the socket is closed, making the receive() method
        // throw an IOException
        this.socket.close();
        super.interrupt();
    }

}
