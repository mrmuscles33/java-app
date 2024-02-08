package fr.amr.filter;

import fr.amr.utils.DateUtils;
import fr.amr.utils.Logger;
import fr.amr.utils.StringUtils;
import fr.amr.utils.StructureUtils;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

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
    private final HttpServletResponse httpResponse;
    private final Map<String, Object> messages = StructureUtils.map("errorMessage", "", "warningMessage", "", "successMessage", "");
    private final Map<String, Object> datas = new HashMap<>();

    /**
     * Constructor
     */
    public JSONResponse(HttpServletResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    /**
     * Add a data in the result
     *
     * @param name  The name of the data
     * @param value The value of the data
     */
    public void setData(String name, Object value) {
        datas.put(name, value);
    }

    /**
     * Catch an error
     *
     * @param msg
     */
    public void setError(String msg) {
        messages.put("errorMessage", msg);
    }

    /**
     * Catch a warning
     *
     * @param msg The warning message
     */
    public void setWarning(String msg) {
        messages.put("warningMessage", msg);
    }

    /**
     * Catch a success
     *
     * @param msg The success message
     */
    public void setSuccess(String msg) {
        messages.put("successMessage", msg);
    }

    /**
     * Get the http response
     *
     * @return The http response
     */
    public HttpServletResponse getHttpResponse() {
        return httpResponse;
    }

    /**
     * Add a header
     *
     * @param name
     * @param value
     */
    public void addHeader(String name, String value) {
        httpResponse.addHeader(name, value);
    }

    /**
     * Add a cookie
     *
     * @param name
     * @param value
     */
    public void addCookie(String name, String value) {
        httpResponse.addCookie(new Cookie(name, value));
    }

    /**
     * Write the result in the response
     */
    public String send() {
        return StringUtils.mapToJson(StructureUtils.map("data", datas, "messages", messages));
    }
}

