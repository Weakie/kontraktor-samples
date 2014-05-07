package samples.niohttp;

import de.ruedigermoeller.kontraktor.Actor;
import de.ruedigermoeller.kontraktor.Actors;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ruedi on 06.05.14.
 */
public class RequestDecoder extends Actor {

    AsyncLogger log;
    RequestProcessor processor;
    ServerSocketChannel socket;
    Selector selector;
    SelectionKey serverkey;
    ByteBuffer buffer = ByteBuffer.allocate(1024*1024);

    public void init( AsyncLogger logger, RequestProcessor processor ) {
        int port = 9999;
        this.processor = processor;
        log = logger;

        try {

            selector = Selector.open();
            socket = ServerSocketChannel.open();
            socket.socket().bind(new java.net.InetSocketAddress(port));
            socket.configureBlocking(false);
            serverkey = socket.register(selector, SelectionKey.OP_ACCEPT);

            log.info("bound to port " + port);
        } catch (IOException e) {
            log.severe("could not bind to port" + port);
            e.printStackTrace();
        }
    }

    public void receive(AtomicInteger receivesUnderway) {
        try {
            receivesUnderway.decrementAndGet();
            int keys = selector.selectNow();;
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
                            iterator.remove();
                            service(key, client);
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            };
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void service(final SelectionKey key, final SocketChannel client) throws IOException {
        int bytesread = client.read(buffer);
        if (bytesread == -1) {
            key.cancel();
            client.close();
        } else {
            buffer.flip();
            Request request = decode(buffer,bytesread);
            processor.processRequest(request,
                (result) -> {
                    try {
                        client.write(ByteBuffer.wrap(result.toString().getBytes()));
                        key.attach((int) key.attachment() + 1);
                        key.cancel();
                        client.close();
                    } catch (Exception e) {
                        e.printStackTrace();
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
        AsyncLogger logger = Actors.SpawnActor(AsyncLogger.class);
        RequestProcessor processor = Actors.SpawnActor(RequestProcessor.class);
        RequestDecoder decoder = Actors.SpawnActor(RequestDecoder.class);

        logger.init();
        decoder.init(logger,processor);
        AtomicInteger receivesUnderway = new AtomicInteger(0);
        while( true ) {
            receivesUnderway.incrementAndGet();
            decoder.receive(receivesUnderway);
            while( receivesUnderway.get() > 2 )
                Thread.yield(); // backoff skipped
        }
    }

}
