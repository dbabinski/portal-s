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
import pl.sportal.jpa.portal.Aktualnosci;

/**
 *
 * @author Damian Babiński <d.babinski94@gmail.com>
 */
@Local
public interface AktualnosciFacadeLocal {

    void create(Aktualnosci object);

    Aktualnosci edit(Aktualnosci object);

    void remove(Aktualnosci object);

    Aktualnosci find(Object id);

    List<Aktualnosci> findAll();

    List<Aktualnosci> findRange(int[] range);

    int count();

    Aktualnosci createManaged(Aktualnosci entity);

    List<Aktualnosci> findPublicated();
    
    void setPrincipal(Principal principal); 
    
    void setClientIpAdress(String clientIpAdress);
    
    static AktualnosciFacadeLocal create(SecurityContext securityContext, String clientIpAdress) throws NamingException {
        return AktualnosciFacadeLocal.create(securityContext != null
                ? securityContext.getUserPrincipal() : (Principal) null, clientIpAdress);
    }

    static AktualnosciFacadeLocal create(Principal principal, String clientIpAdress) throws NamingException {
        InitialContext initialContext = new InitialContext();
        AktualnosciFacadeLocal bean = (AktualnosciFacadeLocal) initialContext
                .lookup("java:app/s-portal-ejb/AktualnosciFacade");
        bean.setPrincipal(principal);
        bean.setClientIpAdress(clientIpAdress);
        return bean;
    }
}
