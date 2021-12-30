package winsome.server.database;

import java.security.SecureRandom;
import java.util.Base64;

public class AuthenticationProvider {
    private final SecureRandom random;
    private int length;

    private final Base64.Encoder encoder = Base64.getUrlEncoder();

    public AuthenticationProvider() {
        this.random = new SecureRandom();
        this.length = 24;
    }

    public AuthenticationProvider(SecureRandom random, int length) {
        this.random = random;
        this.length = length;
    }

    public String generateNewToken() {
        var bytes = new byte[this.length];
        this.random.nextBytes(bytes);
        return encoder.encodeToString(bytes);
    }

}
