package samples.futures;

import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by ruedi on 01.06.14.
 */

public class NodeDotJS {

    // the single main thread
    Executor mailbox = Executors.newSingleThreadExecutor();
    // pool to execute blocking actions
    Executor forBlockers = Executors.newCachedThreadPool();

    // public method to receive requests from outside
    public void receiveRequest( Object req ) {
        // force all requests onto single main thread in
        // Executor mailbox
        mailbox.execute(() -> processRequest(req));
    }

    HashMap someMap;
    int someInt = 1;
    // executed in mailbox-thread only
    private void processRequest(Object req) {
        if ( req instanceof Runnable ) { // assume callback
            ((Runnable) req).run();
        } else if ( req instanceof WorkRequest0) {
            someInt++;
            // ok now lets execute the blocking action in our
            // pool reserved for blocking actions
            forBlockers.execute(() -> {
                // do a blocking call ..
                try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
                // put the result to the queue of our single thread
                // this way we ensure single threaded execution
                mailbox.execute(() -> {
                        System.out.println("done");
                        someMap.get("bla_" + someInt);
                        // yay call back hell, but single threaded
                    }
                );
            });
        }
    }

    public static void main( String arg[] ) {
        new NodeDotJS().receiveRequest( new WorkRequest0() );
    }

}

class WorkRequest0 {}
