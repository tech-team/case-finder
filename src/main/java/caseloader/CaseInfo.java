package caseloader;

import caseloader.kad.Urls;
import org.json.JSONArray;
import org.json.JSONObject;
import util.JsonUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaseInfo {
    abstract class Keys {
        public static final String CASE_ID = "CaseId";
        public static final String CASE_NUMBER = "CaseNumber";
        public static final String CASE_TYPE = "CaseType";
        public static final String CATEGORY_DISPUTE = "CategoryDispute";
        public static final String CATEGORY_DISPUTE_ID = "CategoryDisputeId";
        public static final String COURT_NAME = "CourtName";
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
        public static final String PLAINTIFFS = "Plaintiffs";
        public static final String COUNT = "Count";
        public static final String PARTICIPANTS = "Participants";
        public static final String RESPONDENTS = "Respondents";
    }

    private String caseId = null;
    private String caseNumber = null;
    private String caseType = null;
    private String courtName = null;
    private String date = null;
    private Boolean isSimpleJustice = null;
    private Object judge;
    private List<CaseSide> plaintiffs = new LinkedList<>();
    private Map<String, CaseSide> plaintiffsIndex  = new HashMap<>();
    private List<CaseSide> respondents = new LinkedList<>();
    private Map<String, CaseSide> respondentsIndex = new HashMap<>();

//    private String categoryDispute = null;
//    private String categoryDisputeId = null;
//
//    private String courtTag = null;
//
//    private Integer respondentsCount = null;
//    private Object event = null;
//    private String instanceNumber = null;
//    private Boolean isFinished = null;
//    private String judgeName = null;
//    private String judgeId = null;
//    private Integer plaintiffsCount = null;
//    private Object subscriptionId = null;


    private Double cost = null;

    private CaseInfo() {
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

    public String getCourtName() {
        return courtName;
    }

    public String getDate() {
        if (date == null) {
            return null;
        }
        String outDate = "0";
        Pattern p = Pattern.compile(".*(\\d+).*");
        Matcher m = p.matcher(date);
        if (m.find()) {
            outDate = m.group(1);
        }

        try {
            Date d = new Date(Long.parseLong(outDate));
            return new SimpleDateFormat("dd.MM.YYYY").format(d);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public Boolean isSimpleJustice() {
        return isSimpleJustice;
    }

    public List<CaseSide> getRespondents() {
        return respondents;
    }

    public List<CaseSide> getPlaintiffs() {
        return plaintiffs;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getUrl() {
        return Urls.KAD_CARD + getCaseId();
    }

    public static CaseInfo fromJSON(JSONObject obj) {
        CaseInfo res = new CaseInfo();

        res.caseId = JsonUtils.getString(obj, Keys.CASE_ID);
        res.caseNumber = JsonUtils.getString(obj, Keys.CASE_NUMBER);
        res.caseType = JsonUtils.getString(obj, Keys.CASE_TYPE);
        res.courtName = JsonUtils.getString(obj, Keys.COURT_NAME);
        res.date = JsonUtils.getString(obj, Keys.DATE);
        res.isSimpleJustice = JsonUtils.getBoolean(obj, Keys.IS_SIMPLE_JUSTICE);
        res.judge = JsonUtils.getObject(obj, Keys.JUDGE);

        JSONObject plaintiffsObj = JsonUtils.getJSONObject(obj, Keys.PLAINTIFFS);
        if (plaintiffsObj != null) {
            JSONArray plaintiffsParticipants = JsonUtils.getJSONArray(plaintiffsObj, Keys.PARTICIPANTS);
            for (int i = 0; i < plaintiffsParticipants.length(); ++i) {
                res.addPlaintiff(CaseSide.fromJSON(plaintiffsParticipants.getJSONObject(i)));
            }
        }


        JSONObject respondentsObj = JsonUtils.getJSONObject(obj, Keys.RESPONDENTS);
        if (respondentsObj != null) {
            JSONArray respondentsParticipants = JsonUtils.getJSONArray(respondentsObj, Keys.PARTICIPANTS);
            for (int i = 0; i < respondentsParticipants.length(); ++i) {
                res.addRespondent(CaseSide.fromJSON(respondentsParticipants.getJSONObject(i)));
            }
        }

        return res;
    }

    private void addRespondent(CaseSide caseSide) {
        this.respondents.add(caseSide);
        this.respondentsIndex.put(caseSide.getName(), caseSide);
    }

    private void addPlaintiff(CaseSide caseSide) {
        this.plaintiffs.add(caseSide);
        this.plaintiffsIndex.put(caseSide.getName(), caseSide);
    }

    public void loadAdditionalInfo(JSONObject caseData) {
        Double cost = JsonUtils.getDouble(caseData, "ClaimSum");
        if (cost != null) {
            setCost(cost);
        }

        JSONObject sides = JsonUtils.getJSONObject(caseData, "Sides");
        if (sides != null) {
            JSONArray plaintiffs = sides.getJSONObject("Plaintiffs").getJSONArray("Items");
            for (int i = 0; i < plaintiffs.length(); ++i) {
                JSONObject plaintiff = plaintiffs.getJSONObject(i);
                CaseSide cs = this.plaintiffsIndex.get(plaintiff.getString("Name"));
                cs.setCaseSideInfo(plaintiff);
            }

            JSONArray defendants = sides.getJSONObject("Defendants").getJSONArray("Items");
            for (int i = 0; i < defendants.length(); ++i) {
                JSONObject defendant = defendants.getJSONObject(i);
                CaseSide cs = this.respondentsIndex.get(defendant.getString("Name"));
                cs.setCaseSideInfo(defendant);
            }
        }
    }
}
