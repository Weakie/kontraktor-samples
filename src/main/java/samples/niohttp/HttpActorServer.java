package samples.niohttp;

import de.ruedigermoeller.kontraktor.Actor;
import de.ruedigermoeller.kontraktor.Actors;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.Callable;

/**
 * Created by ruedi on 06.05.14.
 */
public class HttpActorServer extends Actor<HttpActorServer> {

    public static boolean IsSingleThreaded = true;

    AsyncLogger logger;
    RequestProcessor processor;
    ServerSocketChannel socket;
    Selector selector;
    SelectionKey serverkey;
    ByteBuffer buffer = ByteBuffer.allocate(1024*1024);
    int serviceCount = 0;
    long lastSerivceTS = 0;
    int requestsOpen = 0;
    private long lastRequest;
    boolean shouldTerminate = false;

    public void init(int port) {
        // use a separate thread
        logger = Actors.SpawnActor(AsyncLogger.class);
        logger.init();

        if ( IsSingleThreaded ) {
            // use same thread making this a single threaded server
            processor = Actors.AsActor(RequestProcessor.class);
        } else {
            // use a separate thread
            processor = Actors.SpawnActor(RequestProcessor.class);
        }


        try {

            selector = Selector.open();
            socket = ServerSocketChannel.open();
            socket.socket().bind(new java.net.InetSocketAddress(port));
            socket.configureBlocking(false);
            serverkey = socket.register(selector, SelectionKey.OP_ACCEPT);

            logger.info("bound to port " + port);
        } catch (IOException e) {
            logger.severe("could not bind to port" + port);
            e.printStackTrace();
        }
    }

    public void stop() {
        super.stop();
        processor.stop();
        logger.stop();
    }

    public void runService() {
        try {
            int keys = selector.selectNow();
            long now = System.currentTimeMillis();
            for (Iterator<SelectionKey> iterator = selector.selectedKeys().iterator(); iterator.hasNext(); ) {
                SelectionKey key = iterator.next();
                try {
                    if (key == serverkey) {
                        if (key.isAcceptable()) {
                            SocketChannel accept = socket.accept();
                            if (accept != null) {
                                accept.configureBlocking(false);
                                SelectionKey register = accept.register(selector, SelectionKey.OP_READ);
                                register.attach(0);
                            }
                        }
                    } else {
                        SocketChannel client = (SocketChannel) key.channel();
                        if (key.isReadable()) {
                            lastRequest = now;
                            iterator.remove();
                            service(key, client);
                            serviceCount++;
                            long dur = now - lastSerivceTS;
                            if (dur>1000) {
                                logger.info("count "+serviceCount+" dur "+dur);
                                serviceCount = 0;
                                lastSerivceTS = now;
                            }
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            // odd, but how to solve this ?
            if ( now -lastRequest > 100 ) {
                Thread.sleep(1);
            } else if ( now - lastRequest > 1000 ) {
                Thread.sleep(50);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if ( ! shouldTerminate )
            self().runService(); // don't call this.runService() ! needs enqeueing ..
    }

    public void stopService() {
        shouldTerminate = true;
    }

    private void service(final SelectionKey key, final SocketChannel client) throws IOException {
        int bytesread = client.read(buffer);
        if (bytesread == -1) {
            key.cancel();
            client.close();
        } else {
            requestsOpen++;
            buffer.flip();
            Request request = decode(buffer,bytesread);
            processor.processRequest(request,
                (result, error) -> {
                    try {
                        client.write(ByteBuffer.wrap(result.toString().getBytes()));
                        key.attach((int) key.attachment() + 1);
                        key.cancel();
                        client.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        requestsOpen--;
                    }
                }
            );
            buffer.clear();
        }
    }

    private Request decode(ByteBuffer buffer, int len) {
        return new Request(buffer,len);
    }

    public static void main( String arg[] ) throws InterruptedException {

        Actors.SetDefaultQueueSize(50000);

        HttpActorServer decoder = Actors.SpawnActor(HttpActorServer.class);
        decoder.init(9999);
        decoder.runService();
    }

}
