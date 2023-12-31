package fr.amr.filter;

import fr.amr.database.DbMgr;
import fr.amr.structure.Pair;
import fr.amr.utils.Logger;
import fr.amr.utils.StringUtils;
import fr.amr.utils.StructureUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.NamingException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SecurityFilter implements Filter {

    public SecurityFilter() {
        super();
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // Init
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // Init database
        try {
            DbMgr.init();
        } catch (NamingException | SQLException e) {
            throw new RuntimeException(e);
        }
        // Get request and response
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        response.setContentType("application/json");

        // Create JSONRequest/JSONResponse from httpRequest/httpResponse
        JSONRequest jsonRequest = new JSONRequest(httpRequest);
        JSONResponse jsonResponse = new JSONResponse();

        List<Pair> authorizedMethods = new ArrayList<>();

        // Check if session is new or not
        if (StringUtils.isEmpty(Context.get("idSession")) || httpRequest.getSession().isNew()) {
            initContext(httpRequest, jsonRequest);
        } else if (!Context.get("idSession").equals(httpRequest.getSession().getId()) || !Context.get("JSESSIONID").equals(jsonRequest.getCookie("JSESSIONID"))) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        } else {
            authorizedMethods = StructureUtils.objectToList(Context.get("authorizedMethods"));
        }

        // Get class and method to call from request
        String className = httpRequest.getParameter("class");
        String methodName = httpRequest.getParameter("method");

        if (className != null && methodName != null) {
            // Check if class and method are authorized
            if (!authorizedMethods.contains(Pair.of(className, methodName))) {
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }
            try {
                // Initialize class and call method
                Class<?> clazz = Class.forName(className);
                Object instance = clazz.getDeclaredConstructor().newInstance();
                clazz.getMethod(methodName, JSONRequest.class, JSONResponse.class).invoke(instance, jsonRequest, jsonResponse);

                // Send response
                String r = jsonResponse.toString();
                try {
                    response.getWriter().print(r);
                } catch (IOException e) {
                    Logger.log(e);
                }
                response.setContentLength(r.length());
            } catch (ClassNotFoundException e) {
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Class not found");
                return;
            } catch (NoSuchMethodException e) {
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Method not found");
                return;
            } catch (Exception e) {
                httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private static void initContext(HttpServletRequest httpRequest, JSONRequest jsonRequest) {
        // Set session id
        Context.set("idSession", httpRequest.getSession().getId());
        Context.set("JSESSIONID", jsonRequest.getCookie("JSESSIONID"));

        // Add authorized methods
        Context.set("authorizedMethods", SecurityMgr.getAuthorizedMethods(jsonRequest.getCookie("user")));
    }

    @Override
    public void destroy() {
        // Destroy
    }
}