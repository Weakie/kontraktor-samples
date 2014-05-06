package samples;

import de.ruedigermoeller.kontraktor.Actor;
import de.ruedigermoeller.kontraktor.Callback;

/**
 * Created by ruedi on 06.05.14.
 */
public class RequestProcessor extends Actor {

    public void processRequest( Request req, Callback<Response> resp ) {
        resp.receiveResult(new Response(req.getText()), null);
    }

}
