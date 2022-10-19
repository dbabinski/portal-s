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
import pl.sportal.jpa.TypyPowiadomien;

/**
 *
 * @author Damian Babiński <damian.babinski@softmedica.pl>
 */
@Local
public interface TypyPowiadomienFacadeLocal {

    void create(TypyPowiadomien object);

    TypyPowiadomien edit(TypyPowiadomien object);

    void remove(TypyPowiadomien object);

    TypyPowiadomien find(Object id);
    
    TypyPowiadomien findById(Integer id);

    List<TypyPowiadomien> findAll();

    List<TypyPowiadomien> findRange(int[] range);

    int count();

    TypyPowiadomien createManaged(TypyPowiadomien entity);

    boolean czyNazwaUnikalna(Integer id, String opis);
    
    boolean czyOpisUnikalny(Integer id, String opis);
    
    void setPrincipal(Principal principal); 
    
    void setClientIpAdress(String clientIpAdress);
    
    static TypyPowiadomienFacadeLocal create(SecurityContext securityContext, String clientIpAdress) throws NamingException {
        return TypyPowiadomienFacadeLocal.create(securityContext != null
                ? securityContext.getUserPrincipal() : (Principal) null, clientIpAdress);
    }

    static TypyPowiadomienFacadeLocal create(Principal principal, String clientIpAdress) throws NamingException {
        InitialContext initialContext = new InitialContext();
        TypyPowiadomienFacadeLocal bean = (TypyPowiadomienFacadeLocal) initialContext
                .lookup("java:app/s-portal-ejb/TypyPowiadomienFacade");
        bean.setPrincipal(principal);
        bean.setClientIpAdress(clientIpAdress);
        return bean;
    }
}
