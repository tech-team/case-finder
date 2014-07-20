package util;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class JsonUtils {
    public static String getString(JSONObject obj, String key) {
        return obj.isNull(key) ? null : obj.getString(key);
    }

    public static Boolean getBoolean(JSONObject obj, String key) {
        return obj.isNull(key) ? null : obj.getBoolean(key);
    }

    public static Integer getInteger(JSONObject obj, String key) {
        return obj.isNull(key) ? null : obj.getInt(key);
    }

    public static JSONObject getJSONObject(JSONObject obj, String key) {
        return obj.isNull(key) ? null : obj.getJSONObject(key);
    }

    public static JSONArray getJSONArray(JSONObject obj, String key) {
        return obj.isNull(key) ? null : obj.getJSONArray(key);
    }

    public static Object getObject(JSONObject obj, String key) {
        return obj.isNull(key) ? null : obj.get(key);
    }

    public static Double getDouble(JSONObject obj, String key) {
        return obj.isNull(key) ? null : obj.getDouble(key);
    }
}
