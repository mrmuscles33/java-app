package fr.amr.filter;

import fr.amr.structure.Pair;
import fr.amr.utils.Logger;
import fr.amr.utils.StringUtils;
import fr.amr.utils.StructureUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.stream;

public class SecurityFilter implements Filter {

    public static final String ID_SESSION = "id_session";
    public static final String USER_CONNECTED = "user_connected";

    public SecurityFilter() {
        super();
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // Init
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        try {
            // Get request and response
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setContentType("application/json");
            httpResponse.setCharacterEncoding("UTF-8");

            // Prevent DDOS
            if (!SecurityMgr.checkDDos(httpRequest.getRemoteAddr())) {
                // Too many requests
                sendError(httpResponse, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Too many requests");
                return;
            }

            // Init JSON request and response
            JSONRequest jsonRequest = new JSONRequest(httpRequest);
            JSONResponse jsonResponse = new JSONResponse(httpResponse);

            // Check if the session is valid
            if (!isValidSession(jsonRequest, jsonResponse)) return;

            // Get authorized methods
            List<Pair<String, String>> authorizedMethods = StructureUtils.objectToList(Context.get("authorizedMethods"));
            String className = jsonRequest.getParam("class");
            String methodName = jsonRequest.getParam("method");

            // Process request using reflection (class and method)
            if (className != null && methodName != null) {
                // Check if the method is authorized for the user
                if (isAuthorizedMethod(authorizedMethods, className, methodName)) {
                    sendError(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                    return;
                }
                processRequest(jsonRequest, jsonResponse, className, methodName);
            }
            chain.doFilter(request, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check if the session is new or invalid
     *
     * @param jsonRequest
     * @param jsonResponse
     * @return
     * @throws IOException
     */
    private boolean isValidSession(JSONRequest jsonRequest, JSONResponse jsonResponse) throws IOException {
        HttpSession session = jsonRequest.getRequest().getSession();
        String token = jsonRequest.getHeader("JWT-TOKEN");
        SecurityToken securityToken = StringUtils.isEmpty(token) ? new SecurityToken() : new SecurityToken(token);
        if (session.isNew() || StringUtils.isEmpty(Context.get(ID_SESSION)) || StringUtils.isEmpty(securityToken.getParam(ID_SESSION))) {
            // New session
            initSession(jsonRequest, securityToken);
        } else if (!Context.get(ID_SESSION).equals(securityToken.getParam(ID_SESSION)) || !securityToken.isValid()) {
            // Bad session
            sendError(jsonResponse, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return false;
        } else if (securityToken.isExpired()) {
            // Expired session
            session.invalidate();
            return false;
        } else {
            // Existing session
            securityToken.setExpiration(1);
        }
        return true;
    }

    /**
     * Process a request using reflection (class and method)
     *
     * @param jsonRequest  JSONRequest
     * @param jsonResponse JSONResponse
     * @param className    Class name
     * @param methodName   Method name
     * @throws Exception
     */
    private void processRequest(JSONRequest jsonRequest, JSONResponse jsonResponse, String className, String methodName) throws IOException {
        HttpServletResponse httpResponse = jsonResponse.getHttpResponse();
        try {
            Class<?> clazz = Class.forName(className);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            clazz.getMethod(methodName, JSONRequest.class, JSONResponse.class).invoke(instance, jsonRequest, jsonResponse);

            String response = jsonResponse.toString();
            httpResponse.getWriter().print(response);
            httpResponse.setContentLength(response.length());
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            sendError(httpResponse, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            sendError(httpResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request");
        }
    }

    /**
     * Send an error to the client
     *
     * @param jsonResponse JSONResponse
     * @param statusCode   Status code
     * @param message      Message
     * @throws IOException
     */
    private void sendError(JSONResponse jsonResponse, int statusCode, String message) throws IOException {
        sendError(jsonResponse.getHttpResponse(), statusCode, message);
    }

    /**
     * Send an error to the client
     *
     * @param httpResponse HttpServletResponse
     * @param statusCode   Status code
     * @param message      Message
     * @throws IOException
     */
    private void sendError(HttpServletResponse httpResponse, int statusCode, String message) throws IOException {
        httpResponse.sendError(statusCode, message);
    }

    /**
     * Check if a method is authorized
     *
     * @param authorizedMethods Authorized methods
     * @param className         Class name
     * @param methodName        Method name
     * @return True if the method is authorized, false otherwise
     */
    private static boolean isAuthorizedMethod(List<Pair<String, String>> authorizedMethods, String className, String methodName) {
        return authorizedMethods.stream().noneMatch(method ->
                method.getOne().endsWith("." + className) &&
                        (methodName.equals(method.getTwo())) || "*".equals(method.getTwo()));
    }

    /**
     * Initialize context values
     *
     * @param jsonRequest   JSONRequest
     * @param securityToken
     */
    private static void initSession(JSONRequest jsonRequest, SecurityToken securityToken) {
        // Set session id
        String sessionId = jsonRequest.getRequest().getSession().getId();
        String user = jsonRequest.getHeader(USER_CONNECTED);
        Context.set(ID_SESSION, sessionId);
        securityToken.addParam(ID_SESSION, sessionId);
        securityToken.addParam(USER_CONNECTED, user);

        // Add authorized methods
        List<Map<String, String>> result;
        try {
            result = SecurityMgr.getAuthorizedMethods(user);
        } catch (SQLException e) {
            Logger.error(e.getMessage());
            result = new ArrayList<>();
        }
        List<Pair<String, String>> authorizedMethods = result.stream().map(line -> new Pair<>(line.get("FULL_CLASSNAME"), line.get("METHOD"))).toList();
        Context.set("authorizedMethods", authorizedMethods);
    }

    @Override
    public void destroy() {
        // Destroy
    }
}