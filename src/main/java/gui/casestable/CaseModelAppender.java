package gui.casestable;

import caseloader.CaseInfo;
import caseloader.CaseSide;
import caseloader.credentials.Credentials;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.util.Collection;

public class CaseModelAppender implements util.Appendable<CaseInfo> {
    private final ObservableList<CaseModel> casesData;

    public CaseModelAppender(ObservableList<CaseModel> casesData) {
        this.casesData = casesData;
    }

    @Override
    public void append(CaseInfo caseInfo) {
        CaseModel caseModel = new CaseModel();

        caseModel.number.setValue(orElse(caseInfo.getCaseNumber(), ""));
        caseModel.url.setValue(orElse(caseInfo.getUrl(), ""));
        caseModel.createdDate.setValue(orElse(caseInfo.getDate(), ""));

        caseModel.plaintiff.setValue(caseInfo.getPlaintiffs()
                .stream()
                .map(CaseSide::getName)
                .reduce((s1, s2) -> s1 + "\n" + s2)
                .orElse(""));

        caseModel.defendant.setValue(caseInfo.getRespondents()
                .stream()
                .map(CaseSide::getName)
                .reduce((s1, s2) -> s1 + "\n" + s2)
                .orElse(""));

        caseModel.inn.setValue(caseInfo.getRespondents()
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

        caseModel.ogrn.setValue(caseInfo.getRespondents()
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

        caseModel.phone.setValue(caseInfo.getRespondents()
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

        caseModel.fullName.setValue(caseInfo.getRespondents()
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

        caseModel.credentialsLink.setValue(caseInfo.getRespondents()
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
        caseModel.caseType.setValue(orElse(caseInfo.getCaseType(), ""));
        caseModel.court.setValue(orElse(caseInfo.getCourtName(), ""));

        Platform.runLater(() -> casesData.add(caseModel));
    }

    @Override
    public Collection<CaseInfo> getCollection() {
        throw new RuntimeException("No getter for collection here");
    }

    private String orElse(String value, String alternative) {
        if (value != null)
            return value;
        else
            return alternative;
    }
}
