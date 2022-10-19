/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.rest;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import javax.annotation.Priority;
import javax.ejb.EJB;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.ext.Provider;
import pl.sportal.jpa.AktualnieZalogowani;
import pl.sportal.session.AktualnieZalogowaniPortalFacadeLocal;


@Provider
@Priority(Priorities.AUTHENTICATION)
public class RESTAuthenticationResponseFilter implements ContainerResponseFilter {

    @EJB
    private AktualnieZalogowaniPortalFacadeLocal aktualnieZalogowaniPortalFacade;
    
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

        //nie przedłużaj życia ciasteczku do usunięcia
        Map<String, NewCookie> responseCookies = responseContext.getCookies();
        if (responseCookies != null && responseCookies.containsKey(JSONWebTokenBuilder.JWT_COOKIE)) {
            Cookie cookie = responseCookies.get(JSONWebTokenBuilder.JWT_COOKIE);
            if (cookie != null && cookie.getValue().equalsIgnoreCase(JSONWebTokenBuilder.LOGOUT_COOKIE)) {
                return;
            }
        }
        if (responseCookies != null && responseCookies.containsKey(JSONWebTokenBuilder.META_COOKIE)) {
            Cookie cookie = responseCookies.get(JSONWebTokenBuilder.META_COOKIE);
            if (cookie != null && cookie.getValue().equalsIgnoreCase(JSONWebTokenBuilder.LOGOUT_COOKIE)) {
                return;
            }
        }

        //wydłużenie czasu wygaśnięcia ciasteczka
        Map<String, Cookie> requestCookies = requestContext.getCookies();
        if (requestCookies != null && requestCookies.containsKey(JSONWebTokenBuilder.JWT_COOKIE)) {
            Cookie jwtCookie = requestCookies.get(JSONWebTokenBuilder.JWT_COOKIE);
            if (jwtCookie != null) {
                String jwt = jwtCookie.getValue();
                if (jwt != null) {
                    JSONWebTokenBuilder jwtBuilder = new JSONWebTokenBuilder(jwt).updateExpiryDate();
                    Date expiration = jwtBuilder.getClaims().getExpiration();
                    String domain = jwtBuilder.getClaims().getDomain();
                    
                    NewCookie jwtNewCookie = new NewCookie(JSONWebTokenBuilder.JWT_COOKIE, jwtBuilder.build(), "/", domain,
                            NewCookie.DEFAULT_VERSION, null, NewCookie.DEFAULT_MAX_AGE,
                            expiration, false, true);

                    AktualnieZalogowani a = new AktualnieZalogowani()
                            .setUuid(jwtBuilder.getClaims().getUUID())
                            .setDataWygasniecia(jwtNewCookie.getExpiry())
                            .setIp(IpAdress.getClientIpAddr(requestContext))
                            .setEmail(jwtBuilder.getClaims().getEmail());
                    
                    aktualnieZalogowaniPortalFacade.aktualizuj(a);
                    
                    responseContext.getHeaders().add("Set-Cookie", jwtNewCookie);

                    if (requestCookies.containsKey(JSONWebTokenBuilder.META_COOKIE)) {
                        Cookie metaCookie = requestCookies.get(JSONWebTokenBuilder.META_COOKIE);
                        if (metaCookie != null) {
                            NewCookie metaNewCookie = new NewCookie(JSONWebTokenBuilder.META_COOKIE, metaCookie.getValue(), "/", domain,
                                    NewCookie.DEFAULT_VERSION, null, NewCookie.DEFAULT_MAX_AGE,
                                    expiration, false, false);
                            responseContext.getHeaders().add("Set-Cookie", metaNewCookie);
                        }
                    }
                }
            }
        }
    }
}
