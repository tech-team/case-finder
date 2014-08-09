package caseloader;

import caseloader.kad.CourtsInfo;
import org.json.JSONArray;
import org.json.JSONObject;
import util.JsonUtils;

import java.util.ArrayList;
import java.util.List;

public class CaseSearchRequest {
    public enum CaseType {
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
    private JSONObject kadJson = new JSONObject();
    private long minCost = 0;
    private int searchLimit = 0;

    public CaseSearchRequest() {
        kadJson.put(Keys.CASE_TYPE, "");
        kadJson.put(Keys.PAGE, 1);
        kadJson.put(Keys.COUNT, 25);
        kadJson.put(Keys.COURTS, new JSONArray());
        kadJson.put(Keys.DATE_FROM, (Object) null);
        kadJson.put(Keys.DATE_TO, (Object) null);
        kadJson.put(Keys.JUDGES_EX, new JSONArray());
        kadJson.put(Keys.JUDGES, new JSONArray());
        kadJson.put(Keys.SIDES, new JSONArray());
        kadJson.put(Keys.CASES, new JSONArray());
        kadJson.put(Keys.WITH_VKS_INSTANCES, false);
    }

    public CaseSearchRequest(final String[] courtsNames,
                             final String dateFrom,
                             final String dateTo,
                             final CaseType caseType,
                             final Boolean withVKSInstances,
                           /*final String[] judgesIds,
                             final String[] sides,
                             final String[] cases*/
                             final long minCost,
                             final int searchLimit) {
        this();

        if (minCost < 0) {
            throw new RuntimeException("Min cost is wrong. Should be greater than or equal to 0");
        }
        if (!(searchLimit > 0 && searchLimit <= 1000)) {
            throw new RuntimeException("Search limit is wrong. Should be in (0; 1000]");
        }

        this.minCost = minCost;
        this.searchLimit = searchLimit;

        if (caseType != null) {
            kadJson.put(Keys.CASE_TYPE, caseType.toString());
        }

        if (withVKSInstances != null) {
            kadJson.put(Keys.WITH_VKS_INSTANCES, withVKSInstances);
        }

        if (courtsNames != null) {
            for (final String court : courtsNames) {
                kadJson.append(Keys.COURTS, CourtsInfo.getCourtCode(court));
            }
        }

        kadJson.put(Keys.DATE_FROM, dateFrom);
        kadJson.put(Keys.DATE_TO, dateTo);
    }

    public CaseType getCaseType() {
        String caseValue = JsonUtils.getString(kadJson, Keys.CASE_TYPE);
        if (caseValue == null || caseValue.equals(""))
            return null;
        return CaseType.valueOf(caseValue);
    }

    @SuppressWarnings("UnusedDeclaration")
    public int getPage() {
        return kadJson.getInt(Keys.PAGE);
    }

    @SuppressWarnings("UnusedDeclaration")
    public int getCount() {
        return kadJson.getInt(Keys.COUNT);
    }

    public String[] getCourts() {
        if (!CourtsInfo.courtsLoaded())
            return null;
        JSONArray courtsJson = JsonUtils.getJSONArray(kadJson, Keys.COURTS);
        List<String> courts = new ArrayList<>();
        for (int i = 0; i < courtsJson.length(); ++i) {
            String courtId = courtsJson.getString(i);
            courts.add(CourtsInfo.getCourtName(courtId));
        }

        String[] courtsArray = new String[courts.size()];
        return courts.toArray(courtsArray);
    }

    public String getDateFrom() {
        return JsonUtils.getString(kadJson, Keys.DATE_FROM);
    }

    public String getDateTo() {
        return JsonUtils.getString(kadJson, Keys.DATE_TO);
    }

    public Boolean isWithVKSInstances() {
        return JsonUtils.getBoolean(kadJson, Keys.WITH_VKS_INSTANCES);
    }

    public long getMinCost() {
        return minCost;
    }

    public int getSearchLimit() {
        return searchLimit;
    }

    public void setPage(final int page) {
        kadJson.put(Keys.PAGE, page);
    }

    public void setCount(final int count) {
        kadJson.put(Keys.COUNT, count);
    }

    @Override
    public String toString() {
        return kadJson.toString();
    }
}
