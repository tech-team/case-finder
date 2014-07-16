package caseloader;

import caseloader.kad.KadDataEntry;
import caseloader.kad.KadResponseSide;

import java.util.List;

public class CaseInfo {
    private KadDataEntry entry = null;

    public CaseInfo(KadDataEntry entry) {
        this.entry = entry;
    }

    public double getCost() {
        return entry.getCost();
    }

    public String getCaseId() {
        return entry.getItem().getCaseId();
    }

    public String getCaseNumber() {
        return entry.getItem().getCaseNumber();
    }

    public String getCaseType() {
        return entry.getItem().getCaseType();
    }

    public String getCategoryDispute() {
        return entry.getItem().getCategoryDispute();
    }

    public String getCategoryDisputeId() {
        return entry.getItem().getCategoryDisputeId();
    }

    public String getCourt() {
        return entry.getItem().getCourt();
    }

    public String getCourtTag() {
        return entry.getItem().getCourtTag();
    }

    public String getDate() {
        return entry.getItem().getDate();
    }

    public Integer getDefendantsCount() {
        return entry.getItem().getDefendantsCount();
    }

    public Object getEvent() {
        return entry.getItem().getEvent();
    }

    public String getInstanceNumber() {
        return entry.getItem().getInstanceNumber();
    }

    public Boolean isFinished() {
        return entry.getItem().isFinished();
    }

    public Boolean isSimpleJustice() {
        return entry.getItem().isSimpleJustice();
    }

    public String getJudgeName() {
        return entry.getItem().getJudgeName();
    }

    public String getJudgeId() {
        return entry.getItem().getJudgeId();
    }

    public Integer getPlaintiffsCount() {
        return entry.getItem().getPlaintiffsCount();
    }

    public Object getSubscriptionId() {
        return entry.getItem().getSubscriptionId();
    }

    public List<KadResponseSide> getDefendants() {
        return entry.getItem().getDefendants();
    }

    public List<KadResponseSide> getPlaintiffs() {
        return entry.getItem().getPlaintiffs();
    }


}
