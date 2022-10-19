/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.sportal.rest;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import java.security.Key;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
/**
 *
 * @author Damian Babiński <damian.babinski@softmedica.pl>
 */
public class JSONWebTokenBuilder {
    
    public static final String JWT_COOKIE = "s-portal.jwt";
    public static final String META_COOKIE = "s-portal.meta";
    public static final String LOGOUT_COOKIE = "LOGOUT";
    private static String SECRET_KEY = "Y1dMS3JPelN2dGVmbmpKbmo2QjasdasdWXppRVQ1";
    public static final int SESSION_TIME = 5;

    private CustomClaims claims = new CustomClaims();

    //--------------------------------------------------------------------------
    // Konstruktor
    //--------------------------------------------------------------------------
    public JSONWebTokenBuilder() {
    }

    public JSONWebTokenBuilder(String jwt) {
        if (jwt != null) {
            Claims c = Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
                    .parseClaimsJws(jwt).getBody();
            if (c instanceof DefaultClaims) {
                claims = new CustomClaims(((DefaultClaims) c).entrySet());
            }
        }
    }

    //--------------------------------------------------------------------------
    // Metody publiczne
    //--------------------------------------------------------------------------
    public CustomClaims getClaims() {
        return claims;
    }

    public JSONWebTokenBuilder put(String key, Object value) {
        claims.put(key, value);
        return this;
    }

    public JSONWebTokenBuilder put(Claims claims) {
        if (claims != null) {
            for (Map.Entry<String, Object> entry : claims.entrySet()) {
                claims.put(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    public Object get(String key) {
        return claims.get(key);
    }

    public <T extends Object> T get(String key, Class<T> requiredType) {
        return claims.get(key, requiredType);
    }

    /**
     * Przedłużenie EXPIRATION o czas SESSION_TIME
     *
     * @param JWT
     * @return
     */
    public JSONWebTokenBuilder updateExpiryDate() {
        Integer sessionTime = ((CustomClaims) claims).getSessionTime();
        if (sessionTime == null) {
            sessionTime = SESSION_TIME;
        }
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, sessionTime);        
        claims.setExpiration(calendar.getTime());
        return this;
    }

    public String build() {
        //The JWT signature algorithm we will be using to sign the token
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        //We will sign our JWT with our ApiKey secret
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECRET_KEY);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        //Let's set the JWT Claims
        JwtBuilder builder = Jwts.builder().setHeaderParam("typ", "JWT");
        for (Map.Entry<String, Object> claim : claims.entrySet()) {
            builder.claim(claim.getKey(), claim.getValue());
        }
        builder.signWith(signatureAlgorithm, signingKey);

        //Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }
    
}
