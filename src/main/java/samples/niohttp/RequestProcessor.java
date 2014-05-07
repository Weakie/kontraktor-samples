package samples.niohttp;

import de.ruedigermoeller.kontraktor.Actor;
import de.ruedigermoeller.kontraktor.annotations.InThread;

/**
 * Created by ruedi on 06.05.14.
 */
public class RequestProcessor extends Actor {

    public static interface ResponseCallback {
        public void responseReceived( Response resp );
    }

//    one could also use the predefined Callback class, which is faster
//    than the InThread annotation
//    public void processRequest( Request req, Callback<Response> resp ) {
//        resp.receiveResult(new Response(req.getText()), null);
//    }

    public void processRequest( Request req, @InThread ResponseCallback cb) {
        cb.responseReceived(new Response(req.getText()));
    }

}
