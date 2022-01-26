package winsome.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

public class RewardsNotificationListener extends Thread {
    private MulticastSocket socket;

    public RewardsNotificationListener(String groupAddress, int groupPort) throws IOException {
        this.socket = new MulticastSocket(groupPort);
        // join the group
        var group = new InetSocketAddress("239.255.32.32", groupPort);
        // TODO config
        var netIf = NetworkInterface.getByName("wlp3s0");
        socket.joinGroup(group, netIf);
    }

    public void run() {

        var buf = new byte[1024];
        var packet = new DatagramPacket(buf, buf.length);

        while (true) {
            try {
                socket.receive(packet);
            } catch (IOException e) {
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
