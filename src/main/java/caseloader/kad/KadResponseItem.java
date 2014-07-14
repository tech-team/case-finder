package caseloader.kad;

import org.json.JSONArray;
import org.json.JSONObject;
import util.JsonUtils;

import java.util.LinkedList;
import java.util.List;

public class KadResponseItem {
    abstract class Keys {
        public static final String CASE_ID = "CaseId";
        public static final String CASE_NUMBER = "CaseNumber";
        public static final String CASE_TYPE = "CaseType";
        public static final String CATEGORY_DISPUTE = "CategoryDispute";
        public static final String CATEGORY_DISPUTE_ID = "CategoryDisputeId";
        public static final String COURT = "Court";
        public static final String COURT_TAG = "CourtTag";
        public static final String DATE = "Date";
        public static final String DEFENDANTS_COUNT = "DefendantsCount";
        public static final String EVENT = "Event";
        public static final String INSTANCE_NUMBER = "InstanceNumber";
        public static final String IS_FINISHED = "IsFinished";
        public static final String IS_SIMPLE_JUSTICE = "IsSimpleJustice";
        public static final String JUDGE = "Judge";
        public static final String JUDGE_NAME = "Name";
        public static final String JUDGE_ID = "Id";
        public static final String PLAINTIFFS_COUNT = "PlaintiffsCount";
        public static final String SIDES = "Sides";
        public static final String SUBSCRIPTION_ID = "SubscriptionId";
    }

    private String caseId = null;
    private String caseNumber = null;
    private String caseType = null;
    private String categoryDispute = null;
    private String categoryDisputeId = null;
    private String court = null;
    private String courtTag = null;
    private String date = null;
    private Integer defendantsCount = null;
    private Object event = null;
    private String instanceNumber = null;
    private Boolean isFinished = null;
    private Boolean isSimpleJustice = null;
    private String judgeName = null;
    private String judgeId = null;
    private Integer plaintiffsCount = null;
    private List<KadResponseSide> sides = null;
    private Object subscriptionId = null;

    private List<KadResponseSide> defendants = null;
    private List<KadResponseSide> plaintiffs  = null;

    private KadResponseItem() {
    }

    public void splitSides() {
        if (sides == null) {
            return; // TODO: throw an exception
        }

        defendants = new LinkedList<>();
        plaintiffs = new LinkedList<>();

        for (KadResponseSide side : sides) {
            switch (side.getSideType()) {
                case PLAINTIFF:
                    plaintiffs.add(side);
                    break;
                case DEFENDER:
                    defendants.add(side);
                    break;
            }
        }

        sides = null;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public String getCaseType() {
        return caseType;
    }

    public String getCategoryDispute() {
        return categoryDispute;
    }

    public String getCategoryDisputeId() {
        return categoryDisputeId;
    }

    public String getCourt() {
        return court;
    }

    public String getCourtTag() {
        return courtTag;
    }

    public String getDate() {
        return date;
    }

    public Integer getDefendantsCount() {
        return defendantsCount;
    }

    public Object getEvent() {
        return event;
    }

    public String getInstanceNumber() {
        return instanceNumber;
    }

    public Boolean isFinished() {
        return isFinished;
    }

    public Boolean isSimpleJustice() {
        return isSimpleJustice;
    }

    public String getJudgeName() {
        return judgeName;
    }

    public String getJudgeId() {
        return judgeId;
    }

    public Integer getPlaintiffsCount() {
        return plaintiffsCount;
    }

    public List<KadResponseSide> getSides() {
        return sides;
    }

    public Object getSubscriptionId() {
        return subscriptionId;
    }

    public List<KadResponseSide> getDefendants() {
        return defendants;
    }

    public List<KadResponseSide> getPlaintiffs() {
        return plaintiffs;
    }

    public static KadResponseItem fromJSON(JSONObject obj) {
        KadResponseItem res = new KadResponseItem();

        res.caseId = JsonUtils.getString(obj, Keys.CASE_ID);
        res.caseNumber = JsonUtils.getString(obj, Keys.CASE_NUMBER);
        res.caseType = JsonUtils.getString(obj, Keys.CASE_TYPE);
        res.categoryDispute = JsonUtils.getString(obj, Keys.CATEGORY_DISPUTE);
        res.categoryDisputeId = JsonUtils.getString(obj, Keys.CATEGORY_DISPUTE_ID);
        res.court = JsonUtils.getString(obj, Keys.COURT);
        res.courtTag = JsonUtils.getString(obj, Keys.COURT_TAG);
        res.date = JsonUtils.getString(obj, Keys.DATE);
        res.defendantsCount = JsonUtils.getInteger(obj, Keys.DEFENDANTS_COUNT);
        res.event = JsonUtils.getObject(obj, Keys.EVENT);
        res.instanceNumber = JsonUtils.getString(obj, Keys.INSTANCE_NUMBER);
        res.isFinished = JsonUtils.getBoolean(obj, Keys.IS_FINISHED);
        res.isSimpleJustice = JsonUtils.getBoolean(obj, Keys.IS_SIMPLE_JUSTICE);

        JSONObject judge = JsonUtils.getJSONObject(obj, Keys.JUDGE);
        if (judge != null) {
            res.judgeName = JsonUtils.getString(judge, Keys.JUDGE_NAME);
            res.judgeId = JsonUtils.getString(judge, Keys.JUDGE_ID);
        }

        res.plaintiffsCount = JsonUtils.getInteger(obj, Keys.PLAINTIFFS_COUNT);
        res.subscriptionId = JsonUtils.getInteger(obj, Keys.SUBSCRIPTION_ID);

        JSONArray sides = JsonUtils.getJSONArray(obj, Keys.SIDES);

        if (sides != null) {
            res.sides = new LinkedList<>();
            for (int i = 0; i < sides.length(); ++i) {
                res.sides.add(KadResponseSide.fromJSON(sides.getJSONObject(i)));
            }
        }

        return res;
    }
}
