package caseloader;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class CasesData implements util.Appendable<CaseInfo> {
    private List<CaseInfo> cases = new LinkedList<>();
    private int totalCount = 0;

    @Override
    public void append(CaseInfo obj) {
        cases.add(obj);
    }

    @Override
    public Collection<CaseInfo> getCollection() {
        return cases;
    }

    @Override
    public void setTotalCount(int count) {
        totalCount = count;
    }

    @Override
    public int getTotalCount() {
        return totalCount;
    }
}
