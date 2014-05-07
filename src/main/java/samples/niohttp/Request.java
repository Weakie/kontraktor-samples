package samples.niohttp;

import java.nio.ByteBuffer;

/**
 * Created by ruedi on 06.05.14.
 */
public class Request {
    String text;
    byte[] bytes;

    public Request(ByteBuffer buffer, int len) {
        bytes = new byte[len];
        buffer.get(bytes);
        text = new String(bytes);
    }

    public String getText() {
        return text;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
