package caseloader;

import java.util.ArrayList;
import java.util.List;

public class CasesData {
    private int totalCount;
    private List<CaseInfo> cases;

    public CasesData(int casesCount) {
        cases = new ArrayList<>(casesCount);
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
