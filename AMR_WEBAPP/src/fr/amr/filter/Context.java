package fr.amr.filter;

import java.util.HashMap;
import java.util.Map;

public class Context {

    private static final ThreadLocal<Map<String, Object>> data = new ThreadLocal<>();

    public static Map<String, Object> getData() {
        return data.get() == null ? new HashMap<>() : data.get();
    }

    public static Object get(String name) {
        return getData().get(name);
    }

    public static void setData(Map<String, Object> c) {
        data.set(c);
    }

    public static void set(String name, Object value) {
        Map<String, Object> map = getData();
        map.put(name, value);
        setData(map);
    }

    public static void clear() {
        data.remove();
    }
}
