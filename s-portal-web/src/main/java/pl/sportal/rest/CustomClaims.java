/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.sportal.rest;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import java.security.Principal;
import java.util.Date;
import java.util.Map;
import java.util.Set;
/**
 *
 * @author Damian Babiński <damian.babinski@softmedica.pl>
 */
public class CustomClaims extends DefaultClaims implements Principal, ClaimsExt{
    
    public CustomClaims() {
    }

    public CustomClaims(Map<String, Object> map) {
        super(map);
    }

    public CustomClaims(Set<Entry<String, Object>> set) {
        if (set != null) {
            set.forEach(entry -> {
                put(entry.getKey(), entry.getValue());
            });
        }
    }

    @Override
    public String getName() {
        return getUUID();
    }

    @Override
    public String getToken() {
        return super.get(TOKEN, String.class);
    }

    @Override
    public CustomClaims setToken(String token) {
        super.put(TOKEN, token);
        return this;
    }

    @Override
    public String getScope() {
        return super.get(SCOPE, String.class);
    }

    @Override
    public CustomClaims setScope(String scope) {
        super.put(SCOPE, scope);
        return this;
    }

    @Override
    public String getUUID() {
        return super.get(UUID, String.class);
    }

    @Override
    public CustomClaims setUUID(String uuid) {
        super.put(UUID, uuid);
        return this;
    }

    @Override
    public String getEmail() {
        return super.get(EMAIL, String.class);
    }

    @Override
    public CustomClaims setEmail(String email) {
        super.put(EMAIL, email);
        return this;
    }

    @Override
    public String getLogin() {
        return super.get(LOGIN, String.class);
    }

    @Override
    public CustomClaims setLogin(String login) {
        super.put(LOGIN, login);
        return this;
    }

    @Override
    public Date getAuthTime() {
        return super.get(AUTH_TIME, Date.class);
    }

    @Override
    public CustomClaims setAuthTime(Date date) {
        super.put(AUTH_TIME, date != null ? date.getTime() : null);
        return this;
    }

    @Override
    public Integer getSessionTime() {
        return super.get(SESSION_TIME, Integer.class);
    }

    @Override
    public ClaimsExt setSessionTime(Integer sessionTime) {
        super.put(SESSION_TIME, sessionTime);
        return this;
    }

    @Override
    public String getDomain() {
        return super.get(DOMAIN, String.class);
    }

    @Override
    public ClaimsExt setDomain(String domain) {
        super.put(DOMAIN, domain);
        return this;
    }

    @Override
    public String getPermissions() {
        return super.get(PERMISSIONS, String.class);
    }

    @Override
    public ClaimsExt setPermissions(String permissions) {
        super.put(PERMISSIONS, permissions);
        return this;
    }

    /**
     * Metody klasy DefaultClaims zwracają niepoprawne wartości w formacie Date
     *
     * @return Date
     */
    @Override
    public Date getExpiration() {
        if (get(Claims.EXPIRATION) instanceof Long) {
            Long time = new Long(get(Claims.EXPIRATION).toString());
            return new Date(time);
        }
        return null;
    }

    @Override
    public ClaimsExt setExpiration(Date date) {
        super.put(EXPIRATION, date != null ? date.getTime() : null);
        return this;
    }

    /**
     * Metody klasy DefaultClaims zwracają niepoprawne wartości w formacie Date
     *
     * @return Date
     */
    @Override
    public Date getNotBefore() {
        if (get(Claims.NOT_BEFORE) instanceof Long) {
            Long time = new Long(get(Claims.NOT_BEFORE).toString());
            return new Date(time);
        }
        return null;
    }

    @Override
    public ClaimsExt setNotBefore(Date date) {
        super.put(NOT_BEFORE, date != null ? date.getTime() : null);
        return this;
    }

    /**
     * Metody klasy DefaultClaims zwracają niepoprawne wartości w formacie Date
     *
     * @return Date
     */
    @Override
    public Date getIssuedAt() {
        if (get(Claims.ISSUED_AT) instanceof Long) {
            Long time = new Long(get(Claims.ISSUED_AT).toString());
            return new Date(time);
        }
        return null;
    }

    @Override
    public ClaimsExt setIssuedAt(Date date) {
        super.put(ISSUED_AT, date != null ? date.getTime() : null);
        return this;
    }
}
