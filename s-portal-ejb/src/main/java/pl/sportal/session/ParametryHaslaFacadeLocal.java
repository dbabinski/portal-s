/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.session;

import java.security.Principal;
import java.util.LinkedList;
import java.util.List;
import javax.ejb.Local;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.SecurityContext;
import pl.sportal.jpa.ParametryHasla;


@Local
public interface ParametryHaslaFacadeLocal {

    void create(ParametryHasla object);

    ParametryHasla edit(ParametryHasla object);

    void remove(ParametryHasla object);

    ParametryHasla find(Object id);

    List<ParametryHasla> findAll();

    List<ParametryHasla> findRange(int[] range);

    int count();

    ParametryHasla createManaged(ParametryHasla entity);

    ParametryHasla find();

    LinkedList<String> sprawdz(String haslo);

    void setPrincipal(Principal principal);    
    
    void setClientIpAdress(String clientIpAdress);
    
    static ParametryHaslaFacadeLocal create(SecurityContext securityContext, String clientIpAdress) throws NamingException {
        return ParametryHaslaFacadeLocal.create(securityContext != null
                ? securityContext.getUserPrincipal() : (Principal) null, clientIpAdress);
    }

    static ParametryHaslaFacadeLocal create(Principal principal, String clientIpAdress) throws NamingException {
        InitialContext initialContext = new InitialContext();
        ParametryHaslaFacadeLocal bean = (ParametryHaslaFacadeLocal) initialContext
                .lookup("java:app/s-portal-ejb/KontaFacade");
        bean.setPrincipal(principal);
        bean.setClientIpAdress(clientIpAdress);
        return bean;
    }
}
