package fr.amr.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JSONRequest {

    private final HttpServletRequest request;
    private final Map<String, String> parameters;
    private final Map<String, String> cookies;
    private final Map<String, String> headers;

    public JSONRequest(HttpServletRequest httpRequest) {
        request = httpRequest;
        parameters = Collections
                .list(request.getParameterNames())
                .stream()
                .collect(Collectors.toMap(Function.identity(), request::getParameter));
        cookies = Arrays
                .stream(request.getCookies())
                .collect(Collectors.toMap(Cookie::getName, Cookie::getValue));
        headers = Collections.list(request.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(Function.identity(), request::getHeader));
    }

    /**
     * Get a parameter
     *
     * @param name The name of the parameter
     * @return The parameter
     */
    public String getParam(String name) {
        return parameters.get(name);
    }

    /**
     * Get all parameters
     *
     * @return The parameters
     */
    public Map<String, String> getParams() {
        return parameters;
    }

    /**
     * Get a cookie
     *
     * @param name The name of the cookie
     * @return The cookie
     */
    public String getCookie(String name) {
        return cookies.get(name);
    }

    /**
     * Get all cookies
     *
     * @return The cookies
     */
    public Map<String, String> getCookies() {
        return cookies;
    }

    /**
     * Get a header
     *
     * @param name The name of the header
     * @return The header
     */
    public String getHeader(String name) {
        return headers.get(name);
    }

    /**
     * Get all headers
     *
     * @return The headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Get the request
     * @return The request
     */
    public HttpServletRequest getRequest() {
        return request;
    }
}
