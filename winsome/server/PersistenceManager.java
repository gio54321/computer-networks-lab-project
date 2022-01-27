package winsome.server;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import winsome.server.database.Database;

/**
 * Thread subclass that manages the server's persistence.
 * The persistence save interval specifies the interval at which every save is
 * performed. At each interval the entire database is saved.
 * The thread can be interrupted by simply calling the interrupt() method.
 */
public class PersistenceManager extends Thread {
    private Database database;
    // the save interval
    private long databaseSaveIntervalMillis;
    // the path to save the database
    private String dbPath;

    public PersistenceManager(Database database, long databaseSaveIntervalMillis, String dbPath) {
        if (database == null || dbPath == null) {
            throw new NullPointerException();
        }
        this.database = database;
        this.databaseSaveIntervalMillis = databaseSaveIntervalMillis;
        this.dbPath = dbPath;
    }

    public void run() {
        // the thread waits in an infinite loop where it waits for the specified
        // interval and then saves the database
        // if the thread is interrupted, then it simply terminates
        while (true) {
            try {
                Thread.sleep(this.databaseSaveIntervalMillis);
            } catch (InterruptedException e) {
                // if the thread is interrupted, then exit
                return;
            }
            // get exclusive access to database
            this.database.beginExclusive();

            System.out.println("saving database");

            // get a serializable clone of the database
            var serializableDb = this.database.cloneToSerializable();

            // write the db content to dbPath
            var mapper = new ObjectMapper();
            var writer = mapper.writer(new DefaultPrettyPrinter());
            try {
                writer.writeValue(new File(this.dbPath), serializableDb);
            } catch (IOException e) {
                System.out.println("Error saving the database to " + this.dbPath);
                e.printStackTrace();
            }

            System.out.println("finished saving database");

            // release exclusive access to database
            this.database.endExclusive();
        }

    }

}
