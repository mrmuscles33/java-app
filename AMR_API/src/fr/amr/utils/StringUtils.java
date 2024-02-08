package fr.amr.utils;

import jakarta.json.*;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StringUtils {

    private StringUtils() {
        super();
    }

    /**
     * Check if a string is empty
     *
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
     * @param obj          The object to convert
     * @param defaultValue The default value
     * @return The string
     */
    public static String toString(Object obj, String defaultValue) {
        return Objects.toString(obj, defaultValue);
    }

    /**
     * Convert an object to a boolean
     *
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

    /**
     * Encode a string in base64
     *
     * @param message The message to encode
     * @return The encoded message
     */
    public static String encode64(String message) {
        return Base64.getEncoder().encodeToString(message.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decode a base64 message
     *
     * @param message The message to decode
     * @return The decoded message
     */
    public static String decode64(String message) {
        return new String(Base64.getDecoder().decode(message), StandardCharsets.UTF_8);
    }

    /**
     * Crypt a message with a key
     *
     * @param message The message to crypt
     * @param key     The secret key
     * @return The crypted message
     */
    public static String crypt(String message, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Transform a JSON string to a Map
     *
     * @param json The JSON string
     * @return The Map
     */
    public static Map<String, Object> jsonToMap(String json) {
        Map<String, Object> map = new HashMap<>();
        try (JsonReader jsonReader = Json.createReader(new StringReader(json))) {
            JsonObject jsonObject = jsonReader.readObject();
            for (String name : jsonObject.keySet()) {
                map.put(name, jsonObject.get(name));
            }
        }
        return map;
    }

    /**
     * Transform a Map to a JSON string
     *
     * @param map The Map
     * @return The JSON string
     */
    public static String mapToJson(Map<String, Object> map) {
        StringWriter stringWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(stringWriter)) {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                builder.add(entry.getKey(), entry.getValue().toString());
            }
            jsonWriter.writeObject(builder.build());
        }
        return stringWriter.toString();
    }
}
