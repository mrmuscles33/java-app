package fr.amr.filter;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@WebListener
public class SessionListener implements HttpSessionListener {

    private static final List<HttpSession> sessions = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        sessions.add(event.getSession());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        Context.clear();
        sessions.remove(event.getSession());
    }

    public static List<HttpSession> getActiveSessions() {
        return new ArrayList<>(sessions);
    }
}
