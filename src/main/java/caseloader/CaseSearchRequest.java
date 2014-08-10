package caseloader;

import caseloader.kad.CourtsInfo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

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
    private JSONObject kadJson = null;
    private CaseType caseType = null;
    private int page = 1;
    private int count = 25;
    private String[] courtsNames = null;
    private String dateFrom = "";
    private String dateTo = "";
    private Object[] judges = null;
    private Object[] sides = null;
    private Object[] cases = null;
    private boolean withVKSInstances = false;

    private long minCost = 0;
    private int searchLimit = 0;

    public CaseSearchRequest() {

    }

    public CaseSearchRequest(final String[] courtsNames,
                             final String dateFrom,
                             final String dateTo,
                             final CaseType caseType,
                             final Boolean withVKSInstances,
                             final long minCost,
                             final int searchLimit) {
        if (minCost < 0) {
            throw new IllegalArgumentException("Min cost is wrong. Should be greater than or equal to 0");
        }
        if (!(searchLimit > 0 && searchLimit <= 1000)) {
            throw new IllegalArgumentException("Search limit is wrong. Should be in (0; 1000]");
        }

        this.minCost = minCost;
        this.searchLimit = searchLimit;

        this.courtsNames = courtsNames;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        if (withVKSInstances != null)
            this.withVKSInstances = withVKSInstances;
        this.caseType = caseType;
    }

    public CaseType getCaseType() {
        return caseType;
    }

    public int getPage() {
        return page;
    }

    @SuppressWarnings("UnusedDeclaration")
    public int getCount() {
        return count;
    }

    public String[] getCourtsNames() {
        return courtsNames;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    @SuppressWarnings("UnusedDeclaration")
    public Object[] getJudges() {
        return judges;
    }

    @SuppressWarnings("UnusedDeclaration")
    public Object[] getSides() {
        return sides;
    }

    @SuppressWarnings("UnusedDeclaration")
    public Object[] getCases() {
        return cases;
    }

    public boolean isWithVKSInstances() {
        return withVKSInstances;
    }

    public long getMinCost() {
        return minCost;
    }

    public int getSearchLimit() {
        return searchLimit;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public CaseSearchRequest copy() {
        CaseSearchRequest copy = new CaseSearchRequest();
        copy.caseType = caseType;
        copy.page = page;
        copy.count = count;
        if (courtsNames != null)
            copy.courtsNames = Arrays.copyOf(courtsNames, courtsNames.length);
        if (judges != null)
            copy.judges = Arrays.copyOf(judges, judges.length);
        if (cases != null)
            copy.cases = Arrays.copyOf(cases, cases.length);
        if (sides != null)
            copy.sides = Arrays.copyOf(sides, sides.length);
        copy.dateFrom = dateFrom;
        copy.dateTo = dateTo;
        copy.withVKSInstances = withVKSInstances;
        copy.minCost = minCost;
        copy.searchLimit = searchLimit;
        return copy;
    }

    private JSONObject buildJson() {
        if (kadJson == null) {
            kadJson = new JSONObject();

            kadJson.put(Keys.CASE_TYPE, "");
            kadJson.put(Keys.PAGE, page);
            kadJson.put(Keys.COUNT, count);
            kadJson.put(Keys.COURTS, new JSONArray());
            kadJson.put(Keys.DATE_FROM, (Object) null);
            kadJson.put(Keys.DATE_TO, (Object) null);
            if (judges == null) {
                kadJson.put(Keys.JUDGES_EX, new JSONArray());
                kadJson.put(Keys.JUDGES, new JSONArray());
            }
            if (sides == null) {
                kadJson.put(Keys.SIDES, new JSONArray());
            }
            if (cases == null) {
                kadJson.put(Keys.CASES, new JSONArray());
            }

            if (caseType != null) {
                kadJson.put(Keys.CASE_TYPE, caseType.toString());
            }

            kadJson.put(Keys.WITH_VKS_INSTANCES, withVKSInstances);

            if (courtsNames != null) {
                for (final String court : courtsNames) {
                    kadJson.append(Keys.COURTS, CourtsInfo.getCourtCode(court));
                }
            }

            kadJson.put(Keys.DATE_FROM, dateFrom);
            kadJson.put(Keys.DATE_TO, dateTo);
        }
        return kadJson;
    }

    @Override
    public String toString() {
        return buildJson().toString();
    }
}
