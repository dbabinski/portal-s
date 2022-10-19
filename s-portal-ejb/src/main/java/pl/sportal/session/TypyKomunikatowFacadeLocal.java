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
import pl.sportal.jpa.TypyKomunikatow;

/**
 *
 * @author Damian Babiński <d.babinski94@gmail.com>
 */
@Local
public interface TypyKomunikatowFacadeLocal {

    void create(TypyKomunikatow object);

    TypyKomunikatow edit(TypyKomunikatow object);

    void remove(TypyKomunikatow object);

    TypyKomunikatow find(Object id);

    List<TypyKomunikatow> findAll();

    List<TypyKomunikatow> findRange(int[] range);

    int count();

    TypyKomunikatow createManaged(TypyKomunikatow entity);
    
    boolean czyOpisUnikalny(Integer id, String opis);
    
    void setPrincipal(Principal principal); 
    
    void setClientIpAdress(String clientIpAdress);
    
    static TypyKomunikatowFacadeLocal create(SecurityContext securityContext, String clientIpAdress) throws NamingException {
        return TypyKomunikatowFacadeLocal.create(securityContext != null
                ? securityContext.getUserPrincipal() : (Principal) null, clientIpAdress);
    }

    static TypyKomunikatowFacadeLocal create(Principal principal, String clientIpAdress) throws NamingException {
        InitialContext initialContext = new InitialContext();
        TypyKomunikatowFacadeLocal bean = (TypyKomunikatowFacadeLocal) initialContext
                .lookup("java:app/s-portal-ejb/TypyKomunikatowFacade");
        bean.setPrincipal(principal);
        bean.setClientIpAdress(clientIpAdress);
        return bean;
    }
}
