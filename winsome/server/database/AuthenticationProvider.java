package winsome.server.database;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Provider of authTokens.
 * The call to generateNewToken will generate a new auth token string
 * By default the auth tokens are a 32 chars long base64 encoded String
 */
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

    /**
     * Generate a new auth Token
     * 
     * @return the newly generated auth token
     */
    public String generateNewToken() {
        var bytes = new byte[this.length];
        this.random.nextBytes(bytes);
        return encoder.encodeToString(bytes);
    }

}
