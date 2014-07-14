package caseloader.kad;

import org.json.JSONArray;
import org.json.JSONObject;

public class KadSearchRequest {
    enum CaseType {
        A, // Административные
        G, // Гражданские
        B // Банкротные
    }

    abstract class Keys {
        public static final String CASE_TYPE = "CaseType";
        public static final String PAGE = "Page";
        public static final String COUNT = "Count";
        public static final String COURTS = "Courts";
        public static final String DATE_FROM = "DateFrom";
        public static final String DATE_TO = "DateTo";
        public static final String JUDGES_EX = "JudgesEx";
        public static final String JUDGES = "Judges";
        public static final String SIDES = "Sides";
        public static final String CASES = "Cases";
        public static final String WITH_VKS_INSTANCES = "WithVKSInstances";
    }
    private JSONObject json = new JSONObject();

    public KadSearchRequest() {
        json.put(Keys.CASE_TYPE, "");
        json.put(Keys.PAGE, 1);
        json.put(Keys.COUNT, 25);
        json.put(Keys.COURTS, new JSONArray());
        json.put(Keys.DATE_FROM, (Object) null);
        json.put(Keys.DATE_TO, (Object) null);
        json.put(Keys.JUDGES_EX, new JSONArray());
        json.put(Keys.JUDGES, new JSONArray());
        json.put(Keys.SIDES, new JSONArray());
        json.put(Keys.CASES, new JSONArray());
        json.put(Keys.WITH_VKS_INSTANCES, false);
    }

    public KadSearchRequest(final String[] courts,
                            final String dateFrom,
                            final String dateTo,
                            final String[] judgesIds,
                            final CaseType caseType,
                            final Boolean withVKSInstances/*,
                            final String[] sides,
                            final String[] cases*/) {
        this();
        if (caseType != null) {
            json.put(Keys.CASE_TYPE, caseType.toString());
        }

        if (withVKSInstances != null) {
            json.put(Keys.WITH_VKS_INSTANCES, withVKSInstances);
        }

        if (courts != null) {
            for (final String court : courts) {
                json.append(Keys.COURTS, court);
            }
        }

        json.put(Keys.DATE_FROM, dateFrom);
        json.put(Keys.DATE_TO, dateTo);

        if (judgesIds != null) {
            for (final String judgeId : judgesIds) {
                json.append(Keys.JUDGES, judgeId);
            }
        }
    }


    public void setPage(final int page) {
        json.put(Keys.PAGE, page);
    }

    @Override
    public String toString() {
        return json.toString();
    }
}
