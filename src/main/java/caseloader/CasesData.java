package caseloader;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class CasesData implements util.Appendable<CaseInfo> {
    private List<CaseInfo> cases = new LinkedList<>();
    private Integer totalCount = null;

    @Override
    public void append(CaseInfo obj) {
        cases.add(obj);
    }

    @Override
    public Collection<CaseInfo> getCollection() {
        return cases;
    }

    @Override
    public void setTotalCount(Integer count) {
        totalCount = count;
    }

    @Override
    public Integer getTotalCount() {
        return totalCount;
    }
}
