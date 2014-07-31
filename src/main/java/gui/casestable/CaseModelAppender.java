package gui.casestable;

import caseloader.CaseInfo;
import caseloader.CaseSide;
import gui.casestable.CaseModel;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import util.*;

import java.util.Collection;

public class CaseModelAppender implements util.Appendable<CaseInfo> {
    private ObservableList<CaseModel> casesData;

    public CaseModelAppender(ObservableList<CaseModel> casesData) {
        this.casesData = casesData;
    }

    @Override
    public void append(CaseInfo caseInfo) {
        CaseModel caseModel = new CaseModel();

        caseModel.number.setValue(caseInfo.getCaseNumber());
        caseModel.url.setValue(caseInfo.getUrl());
        caseModel.createdDate.setValue(caseInfo.getDate());

        caseModel.plaintiff.setValue(caseInfo.getPlaintiffs()
                .stream()
                .map(CaseSide::getName)
                .reduce((s1, s2) -> s1 + ", " + s2)
                .get());

        caseModel.defendant.setValue(caseInfo.getDefendants()
                .stream()
                .map(CaseSide::getName)
                .reduce((s1, s2) -> s1 + ", " + s2)
                .get());

        caseModel.cost.setValue(caseInfo.getCost());
        caseModel.caseType.setValue(caseInfo.getCaseType());
        caseModel.court.setValue(caseInfo.getCourt());

        Platform.runLater(() -> casesData.add(caseModel));
    }

    @Override
    public Collection<CaseInfo> getCollection() {
        throw new RuntimeException("No getter for collection here");
    }
}