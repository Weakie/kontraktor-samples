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

    public void infoTim(long time, String s) {
        logger.info(new Date(time) + " " + s);
    }

    public void warnTim(long time, String s) {
        logger.warning(new Date(time)+" " + s);
    }

    public void severeTim(long time, String s) {
        logger.severe(new Date(time) + " " + s);
    }

    @CallerSideMethod public void info(String s) {
        infoTim(System.currentTimeMillis(), s);
    }

    @CallerSideMethod public void warn(String s) {
        warnTim(System.currentTimeMillis(), s);
    }

    @CallerSideMethod public void severe(String s) {
        severeTim(System.currentTimeMillis(), s);
    }


}