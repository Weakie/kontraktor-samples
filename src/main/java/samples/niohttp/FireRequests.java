package samples.niohttp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by ruedi on 07.05.14.
 */
public class FireRequests {

    public static void main( String arg[] ) throws IOException {

        int count = 0;
        long tim = System.currentTimeMillis();
        while( true ) {
            String res = new Scanner(new URL("http://localhost:9999/index.html").openStream(), "UTF-8").useDelimiter("\\A").next();
            long dur = System.currentTimeMillis() - tim;
            if ( dur > 1000 ) {
                System.out.println("count: "+count+" dur "+dur);
                count = 0;
                tim = System.currentTimeMillis();
            }
        }
    }
}
