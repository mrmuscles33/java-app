package fr.amr.filter;

import fr.amr.utils.DateUtils;
import fr.amr.utils.Logger;
import fr.amr.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SecurityToken {

    private String token;
    private static final String SECRET = "79iv74smER2SiGB9";
    private final Map<String, Object> params;

    public static final String EXPIRATION = "EXPIRATION";

    public SecurityToken() {
        this(new HashMap<>());
        this.setExpiration(1);
    }

    public SecurityToken(String token) {
        this.token = token;
        this.params = StringUtils.jsonToMap(StringUtils.decode64(token.split("\\.")[0]));
    }

    public SecurityToken(Map<String, Object> params) {
        this.params = params;
        this.sign();
    }

    public void addParam(String name, String value) {
        this.params.put(name, value);
        this.sign();
    }

    public String getParam(String name) {
        return Objects.toString(params.get(name), "");
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public String getToken() {
        return token;
    }

    public void sign() {
        this.token = StringUtils.encode64(StringUtils.mapToJson(params)) + "." + StringUtils.crypt(StringUtils.encode64(StringUtils.mapToJson(params)), SECRET);
    }

    public boolean isValid() {
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            return false;
        }
        return Objects.equals(parts[1], StringUtils.crypt(parts[0], SECRET));
    }

    public boolean isExpired() {
        return DateUtils.now().isAfter(DateUtils.toDateTime(this.getParam(EXPIRATION)));
    }

    public void setExpiration(int hours) {
        this.addParam(EXPIRATION, DateUtils.toString(DateUtils.now().plusHours(hours)));
    }
}
