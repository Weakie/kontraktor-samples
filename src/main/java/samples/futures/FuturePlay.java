package samples.futures;

import de.ruedigermoeller.kontraktor.*;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.Callable;

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
            return new Promise<>(title+" "+tmillis);
        }

    }

    public static class TestBlockingAPI extends Actor<TestBlockingAPI> {

        public Future<String> get( final String url ) {
            Promise<String> content = new Promise();
            Async( () -> new Scanner( new URL(url).openStream(), "UTF-8" ).useDelimiter("\\A").next() )
                .then(content);
            return content;
        }
    }

    public static class SleepActor extends Actor<SleepActor> {

        public Future $sleep(long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return new Promise<>("void");
        }

    }

    public static class MainActor extends Actor<MainActor> {

        SomeWork workA;
        SomeWork workB;

        public void init() {
            workA = SpawnActor(SomeWork.class);
            workB = SpawnActor(SomeWork.class);

            SleepActor timer = SpawnActor(SleepActor.class);
            timer.$sleep(1000).then( (r,e) -> System.out.println("message "+r) );
        }

        private String getURL( String url ) {
            try {
                return new Scanner(new URL(url).openStream(), "UTF-8").useDelimiter("\\A").next();
            } catch (IOException e) { e.printStackTrace(); }
            return null;
        }

        public Future $myFunc() {
            Promise result = new Promise(); // uncompleted promise
            Async( () -> getURL("http://google.com") )
                // other events from the mailbox/queue are processed while request is running
                .map( (response, e) -> {
                    /*process response]*/;
                    return new Promise("processingresult");
                })
                // other events from the mailbox/queue are processed inbetween
                .then( result );
            return result;
        }


        public void main() {
            TestBlockingAPI api = SpawnActor(TestBlockingAPI.class);
            api.get("http://www.google.com").then( (r,e) -> System.out.println(r) );
        }

        @Override
        public void stop() {
            super.stop();
            workA.stop();
            workB.stop();
        }

        public Future testNestedResultHandling( int count ) {
            checkThread();
            Promise result = new Promise();
            if ( count == 0 )
                result.receiveResult("Finally got it", null);
            else {
                System.out.println("exec "+count);
                self().testNestedResultHandling(count-1).then((r,e) -> result.receiveResult(r,e));
            }
            return result;
        }

        public Future run() {
            Promise finalResult = new Promise();
            if ( workA == null ) {
                init();
            }

            workA.doWork("first")
                 .then( (result,error) -> { checkThread(); System.out.println("Yes 0");} )
                 .then( (result,error) -> { checkThread(); System.out.println("Yes 1");} )
                 .then( (result,error) -> { checkThread(); System.out.println("Yes 2");} );


            Message hossa = msg( workA.$().doWork("Hossa ! ") );

            MessageSequence sequence = seq(
                workA.$().doWork("delayedA"),
                workB.$().doWork("delayedB"),
                workA.$().doWork("delayedA 0"),
                workB.$().doWork("delayedB 0"),
                workA.$().doWork("delayedA 1"),
                workB.$().doWork("delayedB 1"),
                workA.$().doWork("delayedA 2"),
                workB.$().doWork("delayedB 2")
            );


            Promise seqFinished = new Promise();

            sequence.exec().then((r, e) -> {
                        checkThread();
                        for (int i = 0; i < r.length; i++) {
                            Future future = r[i];
                            System.out.println(" -- " + future.getResult());
                        }
                        System.out.println("------------ exec finished, start yield ..");
                        sequence.yield().then((r1, e1) -> {
                            checkThread();
                            for (int i = 0; i < r.length; i++) {
                                Future future = r[i];
                                System.out.println(" -- " + future.getResult());
                            }
                            seqFinished.receiveResult(null, null);
                        });
                    }
            );

            seqFinished.then((r, e) -> {
                checkThread();
                ;
                hossa.send().then(finalResult);
            });

            return finalResult;
        }

        void checkThread() {
            if ( Thread.currentThread() != getDispatcher() ) {
                throw new RuntimeException("wrong thread !");
            }
        }

    }

    public static void main(String a[]) {
        MainActor main = SpawnActor(MainActor.class);
        main.run().then( (r,e) -> {
            System.out.println("came back from run");
            main.testNestedResultHandling(10).then( (r1,e1) -> main.stop() );
        });
    }

    public void blog1() {
        Async( () -> {
            try {
                Thread.sleep(1000); // emulate blocking operation
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "message";
        }).then( (r,e) -> {
            System.out.println(r);
            someMap.get("bla_" + someInt);
        });
    }


    HashMap someMap;
    int someInt = 1;
    public void blog() {
        someInt++;
        new Thread( () -> {
            try {
                Thread.sleep(1000); // emulate blocking operation
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("message");
            someMap.get("bla_" + someInt);
        }).start();
    }

}
