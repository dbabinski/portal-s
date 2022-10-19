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
import pl.sportal.jpa.Uprawnienia;

@Local
public interface UprawnieniaFacadeLocal {

    void create(Uprawnienia object);

    Uprawnienia edit(Uprawnienia object);

    void remove(Uprawnienia object);

    Uprawnienia find(Object id);

    List<Uprawnienia> findAll();

    List<Uprawnienia> findRange(int[] range);
    
    Uprawnienia findByIdGrupy (Integer idGrupy);

    int count();

    Uprawnienia createManaged(Uprawnienia entity);
    
    void setPrincipal(Principal principal); 
    
    void setClientIpAdress(String clientIpAdress);
    
    static UprawnieniaFacadeLocal create(SecurityContext securityContext, String clientIpAdress) throws NamingException {
        return UprawnieniaFacadeLocal.create(securityContext != null
                ? securityContext.getUserPrincipal() : (Principal) null, clientIpAdress);
    }

    static UprawnieniaFacadeLocal create(Principal principal, String clientIpAdress) throws NamingException {
        InitialContext initialContext = new InitialContext();
        UprawnieniaFacadeLocal bean = (UprawnieniaFacadeLocal) initialContext
                .lookup("java:app/s-portal-ejb/UprawnieniaFacade");
        bean.setPrincipal(principal);
        bean.setClientIpAdress(clientIpAdress);
        return bean;
    }
}
