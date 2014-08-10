package util.net;

public class MalformedUrlException extends Exception {
    public MalformedUrlException() {
        super("Error happened while retrieving data");
    }

    public MalformedUrlException(String message) {
        super(message);
    }

    public MalformedUrlException(String message, Throwable cause) {
        super(message, cause);
    }

    public MalformedUrlException(Throwable cause) {
        super(cause);
    }
}
