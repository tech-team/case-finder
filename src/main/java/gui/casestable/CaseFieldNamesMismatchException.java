package gui.casestable;

public class CaseFieldNamesMismatchException extends Exception {
    public CaseFieldNamesMismatchException() {
        super();
    }

    public CaseFieldNamesMismatchException(String message) {
        super(message);
    }

    public CaseFieldNamesMismatchException(Exception cause) {
        super(cause);
    }

    public CaseFieldNamesMismatchException(String message, Exception cause) {
        super(message, cause);
    }
}
