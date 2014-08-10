package util.net;

class DataRetrievingError extends Exception {
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
