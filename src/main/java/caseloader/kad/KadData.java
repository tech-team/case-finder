package caseloader.kad;

import java.util.ArrayList;
import java.util.List;

public class KadData {
    private List<KadDataEntry> entries;
    private int totalCount;

    public KadData(int entriesCount, int totalCount) {
        entries = new ArrayList<>(entriesCount);
        this.totalCount = totalCount;
    }

    public List<KadDataEntry> getEntries() {
        return entries;
    }

    public void addEntry(KadDataEntry entry) {
        entries.add(entry);
    }

    public int getTotalCount() {
        return totalCount;
    }
}
