package samples;

import de.ruedigermoeller.kontraktor.Actor;
import de.ruedigermoeller.kontraktor.annotations.CallerSideMethod;

import java.io.IOException;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ruedi on 06.05.14.
 */
public class AsyncLogger extends Actor {

    Logger logger;

    public void init() {
        logger = Logger.getGlobal();
        logger.setLevel(Level.INFO);
        try {
            logger.addHandler( new FileHandler("Logging.txt") );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void info( long time, String s ) {
        logger.info(new Date(time) + " " + s);
    }

    public void warn( long time, String s ) {
        logger.warning(new Date(time)+" " + s);
    }

    public void severe( long time, String s ) {
        logger.severe(new Date(time) + " " + s);
    }

    @CallerSideMethod public void cinfo( String s ) {
        info(System.currentTimeMillis(),s);
    }

    @CallerSideMethod public void cwarn( String s ) {
        warn(System.currentTimeMillis(),s);
    }

    @CallerSideMethod public void csevere( String s ) {
        severe(System.currentTimeMillis(),s);
    }


}
