package fr.amr.filter;

import fr.amr.utils.DateUtils;
import fr.amr.utils.Logger;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class JSONResponse {

    private final JsonObjectBuilder json = Json.createObjectBuilder();

    /**
     * Constructor
     */
    public JSONResponse() {
        super();
    }

    /**
     * Add a data in the result
     *
     * @param name  The name of the data
     * @param value The value of the data
     */
    public void setData(String name, Object value) {
        this.setData(this.json, name, value);
    }

    /**
     * Catch an error
     *
     * @param msg
     */
    public void setError(String msg) {
        setData("errorMessage", msg);
    }

    /**
     * Catch an error
     *
     * @param msg  The error message
     * @param code The error code
     */
    public void setError(String msg, String code) {
        setError(msg);
        setData("errorCode", code);
    }

    /**
     * Catch a warning
     *
     * @param msg The warning message
     */
    public void setWarning(String msg) {
        setData("warningMessage", msg);
    }

    /**
     * Catch a warning
     *
     * @param msg  The warning message
     * @param code The warning code
     */
    public void setWarning(String msg, String code) {
        setWarning(msg);
        setData("warningCode", code);
    }

    /**
     * Catch a success
     *
     * @param msg The success message
     */
    public void setSuccess(String msg) {
        setData("successMessage", msg);
    }

    /**
     * Catch a success
     *
     * @param msg  The success message
     * @param code The success code
     */
    public void setSuccess(String msg, String code) {
        setSuccess(msg);
        setData("successCode", code);
    }

    /**
     * Write the result in the response
     */
    public String send() {
        return json.build().toString();
    }

    /**
     * Add a single data
     *
     * @param pJson The json object
     * @param name  The name of the data
     * @param value The value of the data
     */
    private void setData(JsonObjectBuilder pJson, String name, Object value) {
        if (value == null) {
            pJson.addNull(name);
        } else if (value instanceof BigDecimal bd) {
            pJson.add(name, bd);
        } else if (value instanceof BigInteger bi) {
            pJson.add(name, bi);
        } else if (value instanceof Boolean b) {
            pJson.add(name, b);
        } else if (value instanceof Integer i) {
            pJson.add(name, i);
        } else if (value instanceof Long l) {
            pJson.add(name, l);
        } else if (value instanceof String s) {
            pJson.add(name, s);
        } else if (value instanceof Character c) {
            pJson.add(name, c);
        } else if (value instanceof char[] ca) {
            pJson.add(name, String.valueOf(ca));
        } else if (value instanceof LocalDate ld) {
            pJson.add(name, DateUtils.toString(ld, DateUtils.D_M_Y));
        } else if (value instanceof LocalDateTime ldt) {
            pJson.add(name, DateUtils.toString(ldt, DateUtils.D_M_Y_H_M_S));
        } else if (value instanceof Iterable<?> it) {
            JsonArrayBuilder jsonArray = Json.createArrayBuilder();
            it.forEach(v -> setData(jsonArray, v));
            pJson.add(name, jsonArray);
        } else if (value instanceof Object[] tab) {
            JsonArrayBuilder jsonArray = Json.createArrayBuilder();
            Arrays.stream(tab).forEach(o -> setData(jsonArray, o));
            pJson.add(name, jsonArray);
        } else if (value instanceof Map<?, ?> m) {
            JsonObjectBuilder o = Json.createObjectBuilder();
            m.forEach((key, value1) -> setData(o, key.toString(), value1));
            pJson.add(name, o);
        } else {
            setData(pJson, name, objectToMap(value));
        }
    }

    /**
     * Add multiple data (arrays, lists)
     *
     * @param pJson The json array
     * @param value The value to add
     */
    private void setData(JsonArrayBuilder pJson, Object value) {
        if (value == null) {
            pJson.addNull();
        } else if (value instanceof BigDecimal bd) {
            pJson.add(bd);
        } else if (value instanceof BigInteger bi) {
            pJson.add(bi);
        } else if (value instanceof Boolean b) {
            pJson.add(b);
        } else if (value instanceof Integer i) {
            pJson.add(i);
        } else if (value instanceof Long l) {
            pJson.add(l);
        } else if (value instanceof String s) {
            pJson.add(s);
        } else if (value instanceof Character c) {
            pJson.add(c);
        } else if (value instanceof char[] ca) {
            pJson.add(String.valueOf(ca));
        } else if (value instanceof LocalDate ld) {
            pJson.add(DateUtils.toString(ld, DateUtils.D_M_Y));
        } else if (value instanceof LocalDateTime ldt) {
            pJson.add(DateUtils.toString(ldt, DateUtils.D_M_Y_H_M_S));
        } else if (value instanceof Iterable<?> it) {
            JsonArrayBuilder jsonArray = Json.createArrayBuilder();
            it.forEach(v -> setData(jsonArray, v));
            pJson.add(jsonArray);
        } else if (value instanceof Object[] tab) {
            JsonArrayBuilder jsonArray = Json.createArrayBuilder();
            Arrays.stream(tab).forEach(o -> setData(jsonArray, o));
            pJson.add(jsonArray);
        } else if (value instanceof Map<?, ?> m) {
            JsonObjectBuilder o = Json.createObjectBuilder();
            m.forEach((key, value1) -> {
                String n = key.toString();
                setData(o, n, value1);
            });
            pJson.add(o);
        } else {
            setData(pJson, objectToMap(value));
        }
    }

    /**
     * Convert an Object to a Map<String,Object>
     *
     * @param o The object to convert
     * @return The map
     */
    private Map<String, Object> objectToMap(Object o) {
        final Map<String, Object> map = new HashMap<>();
        // public fields
        List<Field> fields = new ArrayList<>(Arrays.asList(o.getClass().getFields()));
        // private fields
        fields.addAll(Arrays.asList(o.getClass().getDeclaredFields()));
        // private super class fields
        Class<?> superClass = o.getClass().getSuperclass();
        if (superClass != null) {
            fields.addAll(Arrays.asList(superClass.getDeclaredFields()));
        }
        // distinct
        fields = fields.stream().distinct().collect(Collectors.toList());
        // methods (for getters)
        final List<String> methods = Arrays.stream(o.getClass().getMethods()).map(Method::getName).toList();
        fields.forEach(f -> {
            f.setAccessible(true);
            try {
                // get / is / has + PropertyName
                String methodName = f.getName().substring(0, 1).toUpperCase() + (f.getName().length() > 1 ? f.getName().substring(1) : "");
                Object value;
                if (methods.contains("get" + methodName)) {
                    value = o.getClass().getMethod("get" + methodName).invoke(o);
                } else if (methods.contains("is" + methodName)) {
                    value = o.getClass().getMethod("is" + methodName).invoke(o);
                } else if (methods.contains("has" + methodName)) {
                    value = o.getClass().getMethod("has" + methodName).invoke(o);
                } else {
                    value = f.get(o);
                }
                map.put(f.getName(), value);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException |
                     InvocationTargetException e) {
                Logger.log("Impossible de lire la propriété " + f.getName() + " de la classe " + o.getClass().getName());
            }
        });
        return map;
    }
}

