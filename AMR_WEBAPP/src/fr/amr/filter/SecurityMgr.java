package fr.amr.filter;

import fr.amr.database.DbMgr;
import fr.amr.utils.DateUtils;

import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SecurityMgr {

    private static ConcurrentMap<String, Integer> requestCounts = new ConcurrentHashMap<>();
    private static ConcurrentMap<String, Instant> lastRequestTimes = new ConcurrentHashMap<>();

    private SecurityMgr() {
        super();
    }

    /**
     * Load authorized classes and methods for a user using his profiles and groups of profiles
     *
     * @param user Connected user
     * @return Authorized classes and methods
     * @throws SQLException
     */
    public static List<Map<String, String>> getAuthorizedMethods(String user) throws SQLException {
        // TODO : Fonctionne que sous Oracle
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append("DISTINCT SJ.PACKAGE || '.' || SJ.CLASS_NAME FULL_CLASSNAME,SJS.METHOD_NAME ");
        query.append("FROM SYS_JAVACLASS_SECU SJS ");
        query.append("INNER JOIN SYS_JAVACLASS SJ ON SJS.CLASS_NAME = SJ.CLASS_NAME ");
        query.append("INNER JOIN SYS_PROFILE SP ON SJS.PROFILE_ID IN ( ");
        query.append("    SELECT SP.PROFILE_ID FROM DUAL UNION ALL ");
        query.append("    SELECT DISTINCT G.PROFILE_ID_CHILD ");
        query.append("    FROM SYS_PROFILE_GROUP G ");
        query.append("    CONNECT BY PRIOR G.PROFILE_ID_CHILD = G.PROFILE_ID_PARENT ");
        query.append("    START WITH G.PROFILE_ID_PARENT = SP.PROFILE_ID ");
        query.append(") ");
        query.append("INNER JOIN SYS_USER_PROFILE SUP ON SUP.PROFILE_ID = SP.PROFILE_ID ");
        query.append("INNER JOIN SYS_USER SU ON SU.USER_ID = SUP.USER_ID ");
        query.append("WHERE SJS.ACTIVE = 1 ");
        query.append("AND SP.ACTIVE = 1 ");
        query.append("AND COALESCE(SUP.EXPIRE_DATE, ?) >= ? ");
        query.append("AND SU.LOGIN = ? ");

        return DbMgr.getRows(query.toString(), Arrays.asList(DateUtils.now(), DateUtils.now(), user));
    }

    /**
     * Prevent a user from making too many requests (20 requests per second)
     *
     * @param ip
     * @return True if the request is allowed, false otherwise
     */
    public static boolean checkDDos(String ip) {
        Instant now = Instant.now();
        Instant oneSecondAgo = now.minusSeconds(1);
        Instant lastRequestTime = lastRequestTimes.get(ip);
        if (lastRequestTime != null && lastRequestTime.isAfter(oneSecondAgo)) {
            requestCounts.put(ip, requestCounts.getOrDefault(ip, 0) + 1);
        } else {
            requestCounts.put(ip, 1);
        }
        lastRequestTimes.put(ip, now);
        return requestCounts.getOrDefault(ip, 0) < 20;
    }
}
