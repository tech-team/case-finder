package util;

import caseloader.CaseInfo;
import gui.casestable.Case;
import javafx.collections.ObservableList;

import java.util.Collection;

public class CaseModelAppender implements Appendable<CaseInfo> {
    private ObservableList<Case> casesData;

    public CaseModelAppender(ObservableList<Case> casesData) {
        this.casesData = casesData;
    }

    @Override
    public void append(CaseInfo caseInfo) {
        Case caseData = new Case();

        //TODO: copy all the data from caseInfo to caseData
        //(add fields to Case if needed)
        //Case class is a model for JavaFX and for export to Excel

        casesData.add(caseData);
    }

    @Override
    public Collection<CaseInfo> getCollection() {
        throw new RuntimeException("No getter for collection here");
    }


}
