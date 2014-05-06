package samples;

import de.ruedigermoeller.kontraktor.Actor;
import de.ruedigermoeller.kontraktor.Actors;
import de.ruedigermoeller.kontraktor.Callback;
import de.ruedigermoeller.kontraktor.LambdaCB;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Objects;

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

            log.cinfo("bound to port " + port);
        } catch (IOException e) {
            log.csevere("could not bind to port" + port);
            e.printStackTrace();
        }
    }

    public void receive() {
        try {
            int keys = 0;
            selector.selectNow();
            selector.selectedKeys().stream().forEach((key) ->
            {
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
                            service(key, client);
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
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
            log.cinfo("processing request " + request.getText());
            processor.processRequest(request, new Callback<Response>() {
                @Override
                public void receiveResult(Response result, Object error) {
                    try {
                        if ( error != null ) {
                            log.cinfo("Error: Responding ....");
                            client.write(ByteBuffer.wrap("ERROR".getBytes()));
                            key.cancel();
                            client.close();
                        } else {
                            log.cinfo("Responding ....");
                            client.write(ByteBuffer.wrap(result.toString().getBytes()));
                            key.attach((int) key.attachment() + 1);
                            key.cancel();
                            client.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
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
        while( true ) {
            decoder.receive();
            Thread.sleep(100);
        }
    }

}
