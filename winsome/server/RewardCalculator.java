package winsome.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import winsome.server.database.Database;

public class RewardCalculator extends Thread {
    private final long calculationIntervalMillis;
    private Database database;
    private InetAddress multicastAddress;
    private int multicastPort;

    public RewardCalculator(Database database, long calculationIntervalMillis, InetAddress multicastAddress,
            int multicastPort) {
        if (database == null || multicastAddress == null) {
            throw new NullPointerException();
        }
        this.calculationIntervalMillis = calculationIntervalMillis;
        this.database = database;
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
    }

    public void run() {
        try (var socket = new DatagramSocket()) {
            while (true) {
                try {
                    Thread.sleep(this.calculationIntervalMillis);
                } catch (InterruptedException e) {
                    System.out.println("thread interrupted");
                    return;
                }
                // calclate rewards

                // get exclusive access to database aka wait for all the ops
                // to complete
                this.database.beginExclusive();

                System.out.println("calculating rewards...");

                this.database.calculateRewards();

                System.out.println("finished calculating rewards");
                // release exclusive access to database
                this.database.endExclusive();

                // send the multicast signal
                var msg = "rewards";
                var packet = new DatagramPacket(msg.getBytes(), msg.length(), this.multicastAddress,
                        this.multicastPort);
                socket.send(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
