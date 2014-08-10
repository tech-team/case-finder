package util;

public class DataRetrievingError extends IllegalArgumentException {
    public DataRetrievingError() {
        super("Error happened while retrieving data");
    }

    public DataRetrievingError(String message) {
        super(message);
    }

    public DataRetrievingError(String message, Throwable cause) {
        super(message, cause);
    }

    public DataRetrievingError(Throwable cause) {
        super(cause);
    }
}
