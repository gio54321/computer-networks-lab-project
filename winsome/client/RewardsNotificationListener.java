package winsome.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

public class RewardsNotificationListener extends Thread {
    private MulticastSocket socket;

    public RewardsNotificationListener(String groupAddress, int groupPort, String netIfName) throws IOException {
        this.socket = new MulticastSocket(groupPort);
        // join the group
        var group = new InetSocketAddress(groupAddress, groupPort);
        var netIf = NetworkInterface.getByName(netIfName);
        if (netIf == null) {
            System.out.println("ERROR: " + netIfName + " is not a valid network interface name");
        }
        socket.joinGroup(group, netIf);
    }

    public void run() {

        var buf = new byte[1024];
        var packet = new DatagramPacket(buf, buf.length);

        while (true) {
            try {
                socket.receive(packet);
            } catch (IOException e) {
                System.out.println("interrupted");
                // channel has closed, terminate thread
                return;
            }
            var res = new String(packet.getData());
            if (!res.trim().contentEquals("rewards")) {
                System.out.println("ERROR: rewards notification is not correct");
            } else {
                System.out.println("NOTIFICATION: rewards updated");
            }
        }

    }

    @Override
    public void interrupt() {
        this.socket.close();
        super.interrupt();
    }

}