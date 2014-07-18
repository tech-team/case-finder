package util;

import caseloader.CaseInfo;
import gui.casestable.CaseModel;
import javafx.collections.ObservableList;

import java.util.Collection;

public class CaseModelAppender implements Appendable<CaseInfo> {
    private ObservableList<CaseModel> casesData;

    public CaseModelAppender(ObservableList<CaseModel> casesData) {
        this.casesData = casesData;
    }

    @Override
    public void append(CaseInfo caseInfo) {
        CaseModel caseModelData = new CaseModel();

        //TODO: copy all the data from caseInfo to caseData
        //(add fields to Case if needed)
        //Case class is a model for JavaFX and for export to Excel

        casesData.add(caseModelData);
    }

    @Override
    public Collection<CaseInfo> getCollection() {
        throw new RuntimeException("No getter for collection here");
    }


}
