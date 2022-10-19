/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.rest;

import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;


@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class RESTAuthenticationRequestFilter implements ContainerRequestFilter {

    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        Cookie cookie = null;
        Map<String, Cookie> cookies = requestContext.getCookies();
        if (cookies != null && cookies.containsKey(JSONWebTokenBuilder.JWT_COOKIE)) {
            cookie = cookies.get(JSONWebTokenBuilder.JWT_COOKIE);
        }
        if (cookie != null) {
            final Claims claims = new JSONWebTokenBuilder(cookie.getValue()).getClaims();
            if (claims != null && claims.getExpiration() != null) {
                if (claims.getExpiration().compareTo(new Date()) > 0) {
                    requestContext.setSecurityContext(new SecurityContext() {

                        @Override
                        public Principal getUserPrincipal() {
                            return (CustomClaims) claims;
                        }

                        @Override
                        public boolean isUserInRole(String role) {
                            return role != null && role.equals(((CustomClaims) claims).getScope());
                        }

                        @Override
                        public boolean isSecure() {
                            return false;
                        }

                        @Override
                        public String getAuthenticationScheme() {
                            return null;
                        }
                    });
                } else {
                    requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
                }
            } else {
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            }
        } else {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}
