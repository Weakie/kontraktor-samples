package samples.futures;

import de.ruedigermoeller.kontraktor.Actor;
import de.ruedigermoeller.kontraktor.Actors;

import java.util.HashMap;

/**
 * Created by ruedi on 01.06.14.
 */
public class NodeJSWithActors extends Actor<NodeJSWithActors> {

    HashMap someMap;
    int someInt = 1;
    public void receiveWorkRequest( WorkRequest req ) {
        someInt++;
        Actors.Async( () -> {
            // do a blocking call ..
            try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
            return "result";
        }).then( (r,e) -> {
            System.out.println("done");
            someMap.get("bla_" + someInt);
            // yay call back hell, but single threaded
        });
    }

    public static void main( String arg[] ) {
        NodeJSWithActors actor = Actors.SpawnActor(NodeJSWithActors.class);
        actor.receiveWorkRequest( new WorkRequest() );
    }

}

class WorkRequest {}
