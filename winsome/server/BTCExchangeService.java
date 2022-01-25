package winsome.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BTCExchangeService {
    public static double getWincoinToBTCConversionRate() throws IOException {
        var randomOrgUrl = new URL(
                "https://www.random.org/decimal-fractions/?num=1&dec=10&col=2&format=plain&rnd=new");
        var randomOrgConnection = (HttpURLConnection) randomOrgUrl.openConnection();
        randomOrgConnection.setRequestMethod("GET");

        var inputReader = new BufferedReader(new InputStreamReader(randomOrgConnection.getInputStream()));
        var resStr = "";
        String line;
        while ((line = inputReader.readLine()) != null) {
            resStr += line;
        }
        var resRate = Double.parseDouble(resStr);
        System.out.println("Got current BTC/winsome rate: " + resRate);
        return resRate;
    }

}
