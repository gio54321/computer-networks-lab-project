package winsome.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BTCExchangeService {
    /**
     * Get wincoi to BTC conversion rate
     * The conversion is done by making a requet to random.org for a random
     * decimal numeber
     * 
     * @return the conversion rate
     * @throws IOException if there is some error with the request to random.org
     */
    public static double getWincoinToBTCConversionRate() throws IOException {
        // make the request
        var randomOrgUrl = new URL(
                "https://www.random.org/decimal-fractions/?num=1&dec=10&col=2&format=plain&rnd=new");
        var randomOrgConnection = (HttpURLConnection) randomOrgUrl.openConnection();
        randomOrgConnection.setRequestMethod("GET");

        // read the input
        var inputReader = new BufferedReader(new InputStreamReader(randomOrgConnection.getInputStream()));
        var resStr = "";
        String line;
        while ((line = inputReader.readLine()) != null) {
            resStr += line;
        }

        // parse the convertion rate
        var resRate = Double.parseDouble(resStr);
        System.out.println("Got current BTC/winsome rate: " + resRate);
        return resRate;
    }

}
