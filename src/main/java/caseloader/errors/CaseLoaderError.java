package caseloader.errors;

public class CaseLoaderError {
    private ErrorReason reason = null;
    private String description = "";

    public CaseLoaderError(ErrorReason reason) {
        this.reason = reason;
    }

    public CaseLoaderError(ErrorReason reason, String description) {
        this.reason = reason;
        this.description = description;
    }

    public ErrorReason getReason() {
        return reason;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("CaseLoaderError :: %s :: %s", reason.toString(), description);
    }
}
