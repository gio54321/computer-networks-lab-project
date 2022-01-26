package winsome.server;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import winsome.server.database.Database;

public class PersistenceManager extends Thread {
    private Database database;
    private long databaseSaveIntervalMillis;
    private String dbPath;

    public PersistenceManager(Database database, long databaseSaveIntervalMillis, String dbPath) {
        this.database = database;
        this.databaseSaveIntervalMillis = databaseSaveIntervalMillis;
        this.dbPath = dbPath;
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(this.databaseSaveIntervalMillis);
            } catch (InterruptedException e) {
                // TODO remove this print
                System.out.println("thread interrupted");
                return;
            }
            // calclate rewards

            // get exclusive access to database aka wait for all the ops
            // to complete
            this.database.beginExclusive();

            System.out.println("saving database");

            var serializableDb = this.database.cloneToSerializable();
            var mapper = new ObjectMapper();
            var writer = mapper.writer(new DefaultPrettyPrinter());
            try {
                writer.writeValue(new File(dbPath), serializableDb);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            System.out.println("finished saving database");
            // release exclusive access to database
            this.database.endExclusive();
        }

    }

}
