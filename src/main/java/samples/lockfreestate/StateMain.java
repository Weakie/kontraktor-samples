package samples.lockfreestate;

import de.ruedigermoeller.kontraktor.Actors;

/**
 * Created by ruedi on 12.05.14.
 */
public class StateMain {

    public static void main(String arg[]) {
        StateContainingActor state = Actors.SpawnActor(StateContainingActor.class);
        state.init();

        StateAccessor accessor = Actors.SpawnActor(StateAccessor.class);
        accessor.init(state);

        accessor.mainLoop();
    }

}
