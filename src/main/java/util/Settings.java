package util;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;

public abstract class Settings {
    static class Keys {
        static class Pair<T> {
            private String key;
            private T value;

            Pair(String key, T value) {
                this.key = key;
                this.value = value;
            }

            public String getKey() {
                return key;
            }

            public T getValue() {
                return value;
            }
        }
        private static final List<Field> CACHED_FIELDS = new ArrayList<>();
        static {
            Field[] declaredFields = Keys.class.getDeclaredFields();
            for (Field field : declaredFields) {
                if (isPublic(field.getModifiers()) && isStatic(field.getModifiers())) {
                    CACHED_FIELDS.add(field);
                }
            }
        }
        @SuppressWarnings("unchecked")
        public static <T> Pair<T> byName(String key) {
            for (Field field : CACHED_FIELDS) {
                try {
                    Pair<T> prop = (Pair<T>) field.get(null);
                    if (prop.getKey().equals(key)) {
                        return prop;
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        public static List<Pair<?>> getProperties() {
            List<Pair<?>> props = new ArrayList<>();
            for (Field field : CACHED_FIELDS) {
                try {
                    Pair<?> prop = (Pair<?>) field.get(null);
                    props.add(prop);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return props;
        }


        // All default properties go here
        public static final Pair<Integer> LOG_LEVEL = new Pair<>("log_level", 3);
        public static final Pair<String> LOG_LOCATION = new Pair<>("log_location", "log");
    }

    private static Map<String, Object> settings = new HashMap<>();

    static {
        // default set-up
        for (Keys.Pair<?> prop : Keys.getProperties()) {
            settings.put(prop.getKey(), prop.getValue());
        }

        // custom set-up
        ResourceBundle res = ResourceBundle.getBundle("properties.settings", new ResourceControl("UTF-8"));
        List<String> keys = Collections.list(res.getKeys());
        for (String key : keys) {
            if (settings.containsKey(key)) {
                if (settings.get(key) instanceof Integer) {
                    settings.put(key, Integer.parseInt(res.getString(key)));
                } else {
                    settings.put(key, res.getString(key));
                }
            } else {
                throw new RuntimeException("Unknown key: " + key);
            }
        }
    }

    public static Level getLogLevel() {
        Integer level = (Integer) settings.get(Keys.LOG_LEVEL.getKey());
        switch (level) {
            case 0:
                return Level.SEVERE;
            case 1:
                return Level.WARNING;
            case 2:
                return Level.INFO;
            case 3:
                return Level.ALL;
            default:
                throw new RuntimeException("Unexpected value of log level");
        }
    }

    public static String getLogLocation() {
        String location = (String) settings.get(Keys.LOG_LOCATION.getKey());
        if (location.endsWith(File.separator)) {
            location = location.substring(0, location.length() - 1);
        }
        return location;
    }
}
