package util;

import caseloader.CaseInfo;
import gui.casestable.Case;
import javafx.collections.ObservableList;

public class CaseModelAppender {
    private ObservableList<Case> casesData;

    public CaseModelAppender(ObservableList<Case> casesData) {
        this.casesData = casesData;
    }

    public void append(CaseInfo caseInfo) {
        Case caseData = new Case();

        //TODO: copy all the data from caseInfo to caseData
        //(add fields to Case if needed)
        //Case class is a model for JavaFX and for export to Excel

        casesData.add(caseData);
    }
}
