package util;

import caseloader.CaseInfo;
import eventsystem.DataEvent;
import gui.casestable.CaseModel;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

public class CaseModelAppender implements Appendable<CaseInfo> {
    private ObservableList<CaseModel> casesData;
    private Integer totalCasesCount = 0;

    public DataEvent<Integer> totalCasesCountObtained = new DataEvent<>();

    public CaseModelAppender(ObservableList<CaseModel> casesData) {
        this.casesData = casesData;
    }

    @Override
    public void append(CaseInfo caseInfo) {
        CaseModel caseModel = new CaseModel();

        caseModel.number.setValue(caseInfo.getCaseNumber());
        caseModel.url.setValue(caseInfo.getUrl());
        caseModel.createdDate.setValue(caseInfo.getDate());
        caseModel.plaintiff.setValue(StringUtils.join(caseInfo.getPlaintiffs(), ", "));
        caseModel.defendant.setValue(StringUtils.join(caseInfo.getDefendants(), ", "));
        caseModel.cost.setValue(caseInfo.getCost());
        caseModel.caseType.setValue(caseInfo.getCaseType());
        caseModel.court.setValue(caseInfo.getCourt());

        casesData.add(caseModel);
    }

    @Override
    public Collection<CaseInfo> getCollection() {
        throw new RuntimeException("No getter for collection here");
    }

    @Override
    public void setTotalCount(Integer count) {
        totalCasesCount = count;
        totalCasesCountObtained.fire(totalCasesCount);
    }

    @Override
    public Integer getTotalCount() {
        return totalCasesCount;
    }
}
