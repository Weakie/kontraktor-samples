package samples.lockfreestate;

import de.ruedigermoeller.kontraktor.Actor;

import java.util.concurrent.locks.LockSupport;
import static samples.lockfreestate.StateContainingActor.*;

/**
 * Created by ruedi on 12.05.14.
 *
 * A demonstration on how to access data inside an actor from outside without
 * locking. Basically a runnable is scheduled onto the thread of the receiver actor.
 * This runnable then obtains access to internal data (single threaded) and
 * 'pipes' results to the calling actor, who receives results in his thread
 * on his queue.
 *
 * Seems humble, however once remoting is implemented, this is a way to execute
 * larger transactions/code by moving code to the actor not the data to the code.
 *
 * It is possible to implement transactional modification from outside this way.
 *
 */
public class StateAccessor extends Actor<StateAccessor> {

    StateContainingActor stateContainingActor;

    public void init(StateContainingActor acc) {
        stateContainingActor = acc;
    }

    public void mainLoop() {

        // query state and return matching record
        // completely single threaded
        stateContainingActor.executeInActorThread(
            (access, actorImpl, result) -> {

                if ( Thread.currentThread() != stateContainingActor.getDispatcher() )
                    throw new RuntimeException("framework error 0");

                System.out.println("--------------------------------------------");

                // this is executed in stateContainingActor's thread
                // get the data from the accessor and search.
                // one could also modify data safely here.
                DataAccess data = (StateContainingActor.DataAccess) access;
                data.getRecords().forEach( (record) -> {
                    if ( record.getPrc() > 10.5 && record.getName().endsWith("120")) {
                        // to to copy as this is sent to the caller actor
                        // no shared state !
                        result.receiveResult(record.createCopy(), null);
                    }
                });
            },
            (result, error) -> {
                // runs in this actors thread.
                if ( Thread.currentThread() != getDispatcher() )
                    throw new RuntimeException("framework error 1");
                System.out.println(result);
            }
        );

        LockSupport.parkNanos(1000*1000*1000);
        self().mainLoop();
    }

}
