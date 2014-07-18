package caseloader;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class CasesData implements util.Appendable<CaseInfo> {

    private List<CaseInfo> cases = new LinkedList<>();

    @Override
    public void append(CaseInfo obj) {
        cases.add(obj);
    }

    @Override
    public Collection<CaseInfo> getCollection() {
        return cases;
    }
}
