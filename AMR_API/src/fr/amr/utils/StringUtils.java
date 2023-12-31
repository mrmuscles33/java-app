package fr.amr.utils;

import java.util.Objects;

public class StringUtils {

    private StringUtils() {
        super();
    }

    /**
     * Check if a string is empty
     * @param str
     * @return
     */
    public static boolean isEmpty(Object str) {
        return str == null || StringUtils.toString(str).trim().isEmpty();
    }

    /**
     * Convert an object to a string
     * If the object is null, return the empty string
     *
     * @param obj The object to convert
     * @return The string
     */
    public static String toString(Object obj) {
        return toString(obj, "");
    }

    /**
     * Convert an object to a string
     * If the object is null, return the default value
     *
     * @param obj The object to convert
     * @param defaultValue The default value
     * @return The string
     */
    public static String toString(Object obj, String defaultValue) {
        return Objects.toString(obj, defaultValue);
    }

    /**
     * Convert an object to a boolean
     * @param value
     * @return
     */
    public static boolean toBool(Object value) {
        String strValue = toString(value);
        return "true".equalsIgnoreCase(strValue) ||
                "1".equals(strValue) ||
                "on".equalsIgnoreCase(strValue) ||
                "yes".equalsIgnoreCase(strValue) ||
                "y".equalsIgnoreCase(strValue) ||
                "oui".equalsIgnoreCase(strValue) ||
                "o".equalsIgnoreCase(strValue);
    }
}
