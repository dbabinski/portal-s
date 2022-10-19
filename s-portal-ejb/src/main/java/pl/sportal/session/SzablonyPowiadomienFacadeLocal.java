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
import pl.sportal.jpa.SzablonyPowiadomien;

/**
 *
 * @author Damian Babiński <damian.babinski@softmedica.pl>
 */
@Local
public interface SzablonyPowiadomienFacadeLocal {
    
    void create(SzablonyPowiadomien object);

    SzablonyPowiadomien edit(SzablonyPowiadomien object);

    void remove(SzablonyPowiadomien object);

    SzablonyPowiadomien find(Object id);
    
    SzablonyPowiadomien findById(Integer id);

    List<SzablonyPowiadomien> findAll();

    List<SzablonyPowiadomien> findRange(int[] range);

    int count();

    SzablonyPowiadomien createManaged(SzablonyPowiadomien entity);

    boolean czyTrescUnikalna(Integer id, String tresc);
    
    void setPrincipal(Principal principal);    
    
    void setClientIpAdress(String clientIpAdress);
    
    static SzablonyPowiadomienFacadeLocal create(SecurityContext securityContext, String clientIpAdress) throws NamingException {
        return SzablonyPowiadomienFacadeLocal.create(securityContext != null
                ? securityContext.getUserPrincipal() : (Principal) null, clientIpAdress);
    }

    static SzablonyPowiadomienFacadeLocal create(Principal principal, String clientIpAdress) throws NamingException {
        InitialContext initialContext = new InitialContext();
        SzablonyPowiadomienFacadeLocal bean = (SzablonyPowiadomienFacadeLocal) initialContext
                .lookup("java:app/s-portal-ejb/SzablonyPowiadomienFacade");
        bean.setPrincipal(principal);
        bean.setClientIpAdress(clientIpAdress);
        return bean;
    }
}
