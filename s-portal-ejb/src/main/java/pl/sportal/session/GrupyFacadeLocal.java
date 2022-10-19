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
import pl.sportal.jpa.Grupy;

/**
 *
 * @author Lucek
 */
@Local
public interface GrupyFacadeLocal {

    void create(Grupy object);

    Grupy edit(Grupy object);

    void remove(Grupy object);

    Grupy find(Object id);

    List<Grupy> findAll();

    List<Grupy> findRange(int[] range);

    int count();

    Grupy createManaged(Grupy entity);
    
    boolean czyOpisUnikalny(Integer id, String opis);
    
    void setPrincipal(Principal principal);
    
    void setClientIpAdress(String clientIpAdress);
    
    static GrupyFacadeLocal create(SecurityContext securityContext, String clientIpAdress) throws NamingException {
        return GrupyFacadeLocal.create(securityContext != null
                ? securityContext.getUserPrincipal() : (Principal) null, clientIpAdress);
    }

    static GrupyFacadeLocal create(Principal principal, String clientIpAdress) throws NamingException {
        InitialContext initialContext = new InitialContext();
        GrupyFacadeLocal bean = (GrupyFacadeLocal) initialContext
                .lookup("java:app/s-portal-ejb/GrupyFacade");
        bean.setPrincipal(principal);
        bean.setClientIpAdress(clientIpAdress);
        return bean;
    }
}
