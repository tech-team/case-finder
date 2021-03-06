package caseloader.errors;

import util.ResourceControl;

import java.util.ResourceBundle;

public enum ErrorReason {
    KAD_PAGE_ERROR("kadPageError"),
    COURTS_RETRIEVAL_ERROR("courtsRetrievalError"),
    UNEXPECTED_ERROR("unexpectedError");

    private final String descResString;

    ErrorReason(String descResString) {
        this.descResString = descResString;
    }

    public String getLocalizedDescription(String details) {
        ResourceBundle res = ResourceBundle.getBundle("properties.error_strings",
                new ResourceControl("UTF-8"));

        return String.format(res.getString(descResString), details);
    }
}
