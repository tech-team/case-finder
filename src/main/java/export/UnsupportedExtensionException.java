package export;

public class UnsupportedExtensionException extends Exception {
    public UnsupportedExtensionException(Exception cause) {
        super("Unsupported extension", cause);
    }
}
