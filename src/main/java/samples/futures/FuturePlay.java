package samples.futures;

import de.ruedigermoeller.kontraktor.*;

import static de.ruedigermoeller.kontraktor.Actors.*;

/**
 * Created by ruedi on 23.05.14.
 */
public class FuturePlay {

    public static class SomeWork extends Actor<SomeWork> {

        public Future<String> doWork(String title) {
            long tmillis = (long) (1000 * Math.random());
            try {
                Thread.sleep(tmillis);
                System.out.println("did work "+title);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return new Future<>(title+" "+tmillis);
        }

    }

    public static class MainActor extends Actor<MainActor> {

        SomeWork workA;
        SomeWork workB;

        public void init() {
            workA = SpawnActor(SomeWork.class);
            workB = SpawnActor(SomeWork.class);
        }

        @Override
        public void stop() {
            super.stop();
            workA.stop();
            workB.stop();
        }

        public Future run() {
            Future finalResult = new Future();
            if ( workA == null ) {
                init();
            }

            workA.doWork("first")
                 .then( (result,error) -> System.out.println("Yes 0") )
                 .then( (result,error) -> System.out.println("Yes 1") )
                 .then( (result,error) -> System.out.println("Yes 2") );

            workA.$().doWork("Hossa ! ");
            Message first = sequence().first();

            workA.$().doWork("delayedA");
            workB.$().doWork("delayedB");
            workA.$().doWork("delayedA 0");
            workB.$().doWork("delayedB 0");
            workA.$().doWork("delayedA 1");
            workB.$().doWork("delayedB 1");
            workA.$().doWork("delayedA 2");
            workB.$().doWork("delayedB 2");
            MessageSequence sequence = sequence();
            Future seqFinished = new Future();
            sequence.exec().then((r, e) -> {
                        for (int i = 0; i < r.length; i++) {
                            IFuture future = r[i];
                            System.out.println(" -- "+future.getResult());
                        }
                        System.out.println("------------ exec finished, start yield ..");
                        sequence.yield().then((r1, e1) -> {
                            for (int i = 0; i < r.length; i++) {
                                IFuture future = r[i];
                                System.out.println(" -- "+future.getResult());
                            }
                            seqFinished.receiveResult(null,null);
                        } );
                    }
            );
            seqFinished.then((r, e) -> first.send().then(finalResult));
            return finalResult;
        }

    }

    public static void main(String a[]) {
        MainActor main = SpawnActor(MainActor.class);
        main.run().then( (r,e) -> {
            System.out.println("came back from run");
            main.stop();
        });
    }

}
