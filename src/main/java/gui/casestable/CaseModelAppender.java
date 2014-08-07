package gui.casestable;

import caseloader.CaseInfo;
import caseloader.CaseSide;
import caseloader.credentials.Credentials;
import javafx.application.Platform;
import javafx.collections.ObservableList;

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
                .reduce((s1, s2) -> s1 + "\n" + s2)
                .orElse(""));

        caseModel.defendant.setValue(caseInfo.getDefendants()
                .stream()
                .map(CaseSide::getName)
                .reduce((s1, s2) -> s1 + "\n" + s2)
                .orElse(""));

        caseModel.inn.setValue(caseInfo.getDefendants()
                .stream()
                .map(caseSide -> {
                    Credentials credentials = caseSide.getCredentials();
                    if (credentials == null)
                        return "";
                    else
                        return caseSide.getCredentials().getInn();
                })
                .reduce((s1, s2) -> s1 + "\n" + s2)
                .orElse(""));

        caseModel.ogrn.setValue(caseInfo.getDefendants()
                .stream()
                .map(caseSide -> {
                    Credentials credentials = caseSide.getCredentials();
                    if (credentials == null)
                        return "";
                    else
                        return caseSide.getCredentials().getOgrn();
                })
                .reduce((s1, s2) -> s1 + "\n" + s2)
                .orElse(""));

        caseModel.phone.setValue(caseInfo.getDefendants()
                .stream()
                .map(caseSide -> {
                    Credentials credentials = caseSide.getCredentials();
                    if (credentials == null)
                        return "";
                    else
                        return caseSide.getCredentials().getAllTelephones()
                                .stream()
                                .reduce((s1, s2) -> s1 + ", " + s2)
                                .orElse("");
                })
                .reduce((s1, s2) -> s1 + "\n" + s2)
                .orElse(""));

        caseModel.fullName.setValue(caseInfo.getDefendants()
                .stream()
                .map(caseSide -> {
                    Credentials credentials = caseSide.getCredentials();
                    if (credentials == null)
                        return "";
                    else
                        return caseSide.getCredentials().getAllDirectors()
                                .stream()
                                .reduce((s1, s2) -> s1 + ", " + s2)
                                .orElse("");
                })
                .reduce((s1, s2) -> s1 + "\n" + s2)
                .orElse(""));

        caseModel.credentialsLink.setValue(caseInfo.getDefendants()
                .stream()
                .map(caseSide -> {
                    Credentials credentials = caseSide.getCredentials();
                    if (credentials == null)
                        return "";
                    else
                        return caseSide.getCredentials().getLink();
                })
                .reduce((s1, s2) -> s1 + "\n" + s2)
                .orElse(""));

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
