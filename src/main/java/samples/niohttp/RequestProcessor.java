package samples.niohttp;

import de.ruedigermoeller.kontraktor.Actor;
import de.ruedigermoeller.kontraktor.Callback;
import de.ruedigermoeller.kontraktor.annotations.InThread;

/**
 * Created by ruedi on 06.05.14.
 */
public class RequestProcessor extends Actor {

    public void processRequest( Request req, Callback<Response> cb) {
        cb.receiveResult(new Response("HTTP/1.0 200 OK\n\n"+req.getText()),null);
    }

}
