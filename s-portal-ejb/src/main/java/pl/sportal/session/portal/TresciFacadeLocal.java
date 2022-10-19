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
import pl.sportal.jpa.portal.Tresci;

/**
 *
 * @author Damian Babiński <d.babinski94@gmail.com>
 */
@Local
public interface TresciFacadeLocal {

    void create(Tresci object);

    Tresci edit(Tresci object);

    void remove(Tresci object);

    Tresci find();

    Tresci find(Object id);

    List<Tresci> findAll();

    List<Tresci> findRange(int[] range);

    int count();

    Tresci createManaged(Tresci entity);
    
    void setPrincipal(Principal principal); 
    
    void setClientIpAdress(String clientIpAdress);
    
    static TresciFacadeLocal create(SecurityContext securityContext, String clientIpAdress) throws NamingException {
        return TresciFacadeLocal.create(securityContext != null
                ? securityContext.getUserPrincipal() : (Principal) null, clientIpAdress);
    }

    static TresciFacadeLocal create(Principal principal, String clientIpAdress) throws NamingException {
        InitialContext initialContext = new InitialContext();
        TresciFacadeLocal bean = (TresciFacadeLocal) initialContext
                .lookup("java:app/s-portal-ejb/TresciFacade");
        bean.setPrincipal(principal);
        bean.setClientIpAdress(clientIpAdress);
        return bean;
    }
}
