package winsome.server;

import winsome.server.database.Database;

public class RewardCalculator extends Thread {
    private final long calculationIntervalMillis;
    private Database database;

    public RewardCalculator(Database database, long calculationIntervalMillis) {
        this.calculationIntervalMillis = calculationIntervalMillis;
        this.database = database;
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(this.calculationIntervalMillis);
            } catch (InterruptedException e) {
                // TODO remove this print
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
        }

    }

}
