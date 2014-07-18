package caseloader.kad;

import caseloader.CaseInfo;
import org.json.JSONArray;
import org.json.JSONObject;
import util.JsonUtils;

import java.util.LinkedList;
import java.util.List;

public class KadResponse {
    abstract class Keys {
        public static final String MESSAGE = "Message";
        public static final String SERVER_DATE = "ServerDate";
        public static final String SUCCESS = "Success";
        public static final String RESULT = "Result";
        public static final String ITEMS = "Items";
        public static final String PAGE = "Page";
        public static final String PAGE_SIZE = "PageSize";
        public static final String PAGES_COUNT = "PagesCount";
        public static final String TOTAL_COUNT = "TotalCount";
    }

    private String message = null;
    private String serverDate = null;
    private Boolean success = null;
    private Integer page = null;
    private Integer pageSize = null;
    private Integer pagesCount = null;
    private Integer totalCount = null;
    private List<CaseInfo> items = null;

    private KadResponse() {

    }

    public String getMessage() {
        return message;
    }

    public String getServerDate() {
        return serverDate;
    }

    public Boolean isSuccess() {
        return success;
    }

    public Integer getPage() {
        return page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public Integer getPagesCount() {
        return pagesCount;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public List<CaseInfo> getItems() {
        return items;
    }

    public static KadResponse fromJSON(JSONObject obj) {
        KadResponse res = new KadResponse();
        res.message = JsonUtils.getString(obj, Keys.MESSAGE);
        res.serverDate = JsonUtils.getString(obj, Keys.SERVER_DATE);
        res.success = JsonUtils.getBoolean(obj, Keys.SUCCESS);

        JSONObject result = JsonUtils.getJSONObject(obj, Keys.RESULT);
        if (result != null) {
            res.page = JsonUtils.getInteger(result, Keys.PAGE);
            res.pageSize = JsonUtils.getInteger(result, Keys.PAGE_SIZE);
            res.pagesCount = JsonUtils.getInteger(result, Keys.PAGES_COUNT);
            res.totalCount = JsonUtils.getInteger(result, Keys.TOTAL_COUNT);

            JSONArray items = JsonUtils.getJSONArray(result, Keys.ITEMS);

            if (items != null) {
                res.items = new LinkedList<>();
                for (int i = 0; i < items.length(); ++i) {
                    res.items.add(CaseInfo.fromJSON(items.getJSONObject(i)));
                }
            }
        }
        return res;
    }
}

