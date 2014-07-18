package caseloader;

import caseloader.kad.KadData;
import caseloader.kad.KadDataEntry;

import java.util.ArrayList;
import java.util.List;

public class CasesData {
    private int totalCount;
    private List<CaseInfo> cases;

    public CasesData(KadData kadData) {
        cases = new ArrayList<>(kadData.getEntries().size());
        setTotalCount(kadData.getTotalCount());
        for (KadDataEntry entry : kadData.getEntries()) {
            CaseInfo caseInfo = new CaseInfo(entry);
            addCase(caseInfo);
        }
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public List<CaseInfo> getCases() {
        return cases;
    }

    public void addCase(CaseInfo caseInfo) {
        cases.add(caseInfo);
    }
}
