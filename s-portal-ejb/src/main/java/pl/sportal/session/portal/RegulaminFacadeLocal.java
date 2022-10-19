/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.session.portal;

import java.security.Principal;
import java.util.List;
import javax.ejb.Local;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.SecurityContext;
import pl.sportal.jpa.portal.Regulamin;

@Local
public interface RegulaminFacadeLocal {

    void create(Regulamin object);

    Regulamin edit(Regulamin object);

    void remove(Regulamin object);

    Regulamin find(Object id);

    List<Regulamin> findAll();

    List<Regulamin> findRange(int[] range);

    int count();

    Regulamin createManaged(Regulamin entity);

    Regulamin findLast();
    
    void setPrincipal(Principal principal); 
    
    void setClientIpAdress(String clientIpAdress);
    
    static RegulaminFacadeLocal create(SecurityContext securityContext, String clientIpAdress) throws NamingException {
        return RegulaminFacadeLocal.create(securityContext != null
                ? securityContext.getUserPrincipal() : (Principal) null, clientIpAdress);
    }

    static RegulaminFacadeLocal create(Principal principal, String clientIpAdress) throws NamingException {
        InitialContext initialContext = new InitialContext();
        RegulaminFacadeLocal bean = (RegulaminFacadeLocal) initialContext
                .lookup("java:app/s-portal-ejb/RegulaminFacade");
        bean.setPrincipal(principal);
        bean.setClientIpAdress(clientIpAdress);
        return bean;
    }
}
