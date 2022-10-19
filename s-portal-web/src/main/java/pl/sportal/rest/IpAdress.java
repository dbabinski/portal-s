/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.rest;

import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;

/**
 *
 * @author Damian Babiński <d.babinski94@gmail.com>
 */
public class IpAdress {
    
    private static final String[] HEADERS_TO_TRY = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR" };
    
    
    public static String getClientIpAddr(HttpServletRequest httpRequest) { 
        for (String header : HEADERS_TO_TRY) {
            String ip = httpRequest.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        
        return httpRequest.getRemoteAddr();
    }
    
    public static String getClientIpAddr(ContainerRequestContext requestContext) { 
        
        for (String header : HEADERS_TO_TRY) {
            List<String> ip = requestContext.getHeaders().get(header);
            if(ip != null){ 
                for(String s : ip){
                    if (s != null && s.length() != 0 && !"unknown".equalsIgnoreCase(s)) {
                        return s;
                    }
                }
            }
        }
        return "";
    }
    
}
