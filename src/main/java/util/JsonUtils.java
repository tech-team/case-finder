package util;

import com.sun.istack.internal.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class JsonUtils {
    public static String getString(@NotNull JSONObject obj, @NotNull String key) {
        return obj.isNull(key) ? null : obj.getString(key);
    }

    public static Boolean getBoolean(@NotNull JSONObject obj, @NotNull String key) {
        return obj.isNull(key) ? null : obj.getBoolean(key);
    }

    public static Integer getInteger(@NotNull JSONObject obj, @NotNull String key) {
        return obj.isNull(key) ? null : obj.getInt(key);
    }

    public static JSONObject getJSONObject(@NotNull JSONObject obj, @NotNull String key) {
        return obj.isNull(key) ? null : obj.getJSONObject(key);
    }

    public static JSONArray getJSONArray(@NotNull JSONObject obj, @NotNull String key) {
        return obj.isNull(key) ? null : obj.getJSONArray(key);
    }

    public static Object getObject(@NotNull JSONObject obj, @NotNull String key) {
        return obj.isNull(key) ? null : obj.get(key);
    }

    public static Double getDouble(@NotNull JSONObject obj, @NotNull String key) {
        return obj.isNull(key) ? null : obj.getDouble(key);
    }
}
