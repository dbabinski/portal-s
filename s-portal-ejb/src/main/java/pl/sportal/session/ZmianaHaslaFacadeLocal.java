/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.session;

import java.security.Principal;
import java.util.List;
import javax.ejb.Local;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.SecurityContext;
import pl.sportal.jpa.ZmianaHasla;


@Local
public interface ZmianaHaslaFacadeLocal {

    void create(ZmianaHasla object);

    ZmianaHasla edit(ZmianaHasla object);

    void remove(ZmianaHasla object);

    ZmianaHasla find(Object id);

    List<ZmianaHasla> findAll();

    List<ZmianaHasla> findRange(int[] range);

    int count();

    ZmianaHasla createManaged(ZmianaHasla entity);
    
    ZmianaHasla findByUUID(String uuid);
    
    ZmianaHasla findByToken(String token);
    
    void setPrincipal(Principal principal); 
    
    void setClientIpAdress(String clientIpAdress);
    
    static ZmianaHaslaFacadeLocal create(SecurityContext securityContext, String clientIpAdress) throws NamingException {
        return ZmianaHaslaFacadeLocal.create(securityContext != null
                ? securityContext.getUserPrincipal() : (Principal) null, clientIpAdress);
    }

    static ZmianaHaslaFacadeLocal create(Principal principal, String clientIpAdress) throws NamingException {
        InitialContext initialContext = new InitialContext();
        ZmianaHaslaFacadeLocal bean = (ZmianaHaslaFacadeLocal) initialContext
                .lookup("java:app/s-portal-ejb/ZmianaHaslaFacade");
        bean.setPrincipal(principal);
        bean.setClientIpAdress(clientIpAdress);
        return bean;
    }
}
