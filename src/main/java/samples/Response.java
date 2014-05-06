package samples;

/**
 * Created by ruedi on 06.05.14.
 */
public class Response {

    private final String text;

    public Response(String text) {
        this.text = text;
    }

    public byte[] getBytes() {
        return text.getBytes();
    }

    @Override
    public String toString() {
        return text;
    }
}
