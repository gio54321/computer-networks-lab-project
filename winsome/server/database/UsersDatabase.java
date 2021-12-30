package winsome.server.database;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UsersDatabase {

    // TODO thread safe?
    List<User> users;

    public UsersDatabase() {
        this.users = new ArrayList<>();
    }

    public void loadFromFile(File f) {

    }

    public synchronized String getUsernameFromId(int userId) {
        return this.users.get(userId).getUsername();
    }

    public synchronized int addNewUserToDatabase(User newUser) {
        // TODO maybe there is a better way? O(n) seems too much
        // check for collisions
        for (User u : this.users) {
            if (u.getUsername() == newUser.getUsername()) {
                return -1;
            }
        }

        // add the user to the db
        this.users.add(newUser);

        // return the user's id
        return this.users.size() - 1;
    }
}
