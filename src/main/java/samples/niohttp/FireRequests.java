package samples.niohttp;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by ruedi on 07.05.14.
 */
public class FireRequests {

    public static void main( String arg[] ) throws IOException {

        Runnable requestor = () -> {
            int count = 0;
            long tim = System.currentTimeMillis();
            while( true ) {
                try {
                    InputStream source = new URL("http://localhost:9999/index.html").openStream();
                    String res = new Scanner(source, "UTF-8").useDelimiter("\\A").next();
                    source.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                }
                long dur = System.currentTimeMillis() - tim;
                count++;
                if ( dur > 1000 ) {
                    System.out.println("count: "+count+" dur "+dur);
                    count = 0;
                    tim = System.currentTimeMillis();
                }
            }
        };

        Executor ex = Executors.newCachedThreadPool();
        ex.execute(requestor);
    }
}
