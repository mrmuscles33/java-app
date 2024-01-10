package fr.amr.utils;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StructureUtils {

    private StructureUtils() {
        super();
    }

    /**
     * Sort a map by key
     *
     * @param map The map to sort
     * @return The sorted map
     */
    public static Map<String, Object> sortMap(Map<String, Object> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
    }

    /**
     * Convert a JsonObject to a Map
     *
     * @param json The JsonObject to convert
     * @return The Map
     */
    public static Map<String, Object> jsonObjToMap(JsonObject json) {
        return json.keySet().stream().collect(Collectors.toMap(Function.identity(), k -> {
            if (json.get(k) instanceof JsonObject) {
                return jsonObjToMap(json.getJsonObject(k));
            } else if (json.get(k) instanceof JsonArray) {
                return jsonArrToList(json.getJsonArray(k));
            } else {
                return json.get(k);
            }
        }));
    }

    /**
     * Convert a JsonArray to a List
     *
     * @param json The JsonArray to convert
     * @return The List
     */
    public static List<Object> jsonArrToList(JsonArray json) {
        return json.stream().map(item -> {
            if (item instanceof JsonObject) {
                return jsonObjToMap((JsonObject) item);
            } else if (item instanceof JsonArray) {
                return jsonArrToList((JsonArray) item);
            } else {
                return item;
            }
        }).collect(Collectors.toList());
    }

    /**
     * Cast an object to a List<T>
     *
     * @param obj The object to convert
     * @return The list
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> objectToList(Object obj) {
        if (obj instanceof List) {
            return (List<T>) obj;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Return a map from the given arguments like Map.of() without 10 arguments limit
     *
     * @param args The arguments
     * @return The map
     */
    public static Map<?, ?> map(Object ...args) {
        Map<Object, Object> map = new HashMap<>();
        for (int i = 0; i < args.length - 1; i += 2) {
            map.put(args[i], args[i + 1]);
        }
        return map;
    }
}
