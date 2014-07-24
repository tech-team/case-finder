package export;

public class ExportException extends Exception {
    public ExportException(Exception cause) {
        super("Export exception", cause);
    }
}
