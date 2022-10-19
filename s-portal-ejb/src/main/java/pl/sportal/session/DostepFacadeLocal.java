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
import pl.sportal.jpa.Dostep;


@Local
public interface DostepFacadeLocal {

    void create(Dostep object);

    Dostep edit(Dostep object);

    void remove(Dostep object);

    Dostep find(Object id);
    
    Dostep find();

    List<Dostep> findAll();

    List<Dostep> findRange(int[] range);

    int count();

    Dostep createManaged(Dostep entity);
    
    void setPrincipal(Principal principal);
    
    void setClientIpAdress(String clientIpAdress);
    
    static DostepFacadeLocal create(SecurityContext securityContext, String clientIpAdress) throws NamingException {
        return DostepFacadeLocal.create(securityContext != null
                ? securityContext.getUserPrincipal() : (Principal) null, clientIpAdress);
    }

    static DostepFacadeLocal create(Principal principal, String clientIpAdress) throws NamingException {
        InitialContext initialContext = new InitialContext();
        DostepFacadeLocal bean = (DostepFacadeLocal) initialContext
                .lookup("java:app/s-portal-ejb/DostepFacade");
        bean.setPrincipal(principal);
        bean.setClientIpAdress(clientIpAdress);
        return bean;
    }

}
