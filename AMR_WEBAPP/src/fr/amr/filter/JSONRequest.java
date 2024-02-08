package fr.amr.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JSONRequest {

    private final HttpServletRequest request;

    public JSONRequest(HttpServletRequest httpRequest) {
        request = httpRequest;
    }

    /**
     * Get a parameter
     *
     * @param name The name of the parameter
     * @return The parameter
     */
    public String getParam(String name) {
        return getParams().get(name);
    }

    /**
     * Get all parameters
     *
     * @return The parameters
     */
    public Map<String, String> getParams() {
        return Collections
                .list(request.getParameterNames())
                .stream()
                .collect(Collectors.toMap(Function.identity(), request::getParameter));
    }

    /**
     * Get a cookie
     *
     * @param name The name of the cookie
     * @return The cookie
     */
    public String getCookie(String name) {
        return getCookies().get(name);
    }

    /**
     * Get all cookies
     *
     * @return The cookies
     */
    public Map<String, String> getCookies() {
        return Arrays
                .stream(request.getCookies())
                .collect(Collectors.toMap(Cookie::getName, Cookie::getValue));
    }

    /**
     * Get a header
     *
     * @param name The name of the header
     * @return The header
     */
    public String getHeader(String name) {
        return getHeaders().get(name);
    }

    /**
     * Get all headers
     *
     * @return The headers
     */
    public Map<String, String> getHeaders() {
        return
                Collections.list(request.getHeaderNames())
                        .stream()
                        .collect(Collectors.toMap(Function.identity(), request::getHeader));
    }

    /**
     * Get the request
     *
     * @return The request
     */
    public HttpServletRequest getRequest() {
        return request;
    }
}
