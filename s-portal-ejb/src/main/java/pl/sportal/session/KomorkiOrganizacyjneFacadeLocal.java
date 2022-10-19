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
import pl.sportal.jpa.KomorkiOrganizacyjne;

/**
 *
 * @author Damian Babiński <damian.babinski@softmedica.pl>
 */

@Local
public interface KomorkiOrganizacyjneFacadeLocal {

    void create(KomorkiOrganizacyjne object);

    KomorkiOrganizacyjne edit(KomorkiOrganizacyjne object);

    void remove(KomorkiOrganizacyjne object);

    KomorkiOrganizacyjne find(Object id);

    List<KomorkiOrganizacyjne> findAll();

    List<KomorkiOrganizacyjne> findRange(int[] range);

    int count();

    KomorkiOrganizacyjne createManaged(KomorkiOrganizacyjne entity);

    boolean czyNazwaUnikalna(Integer id, String nazwa);

    boolean czyMogeUsunacKomorkeOrganizacyjna(int id);

    void setPrincipal(Principal principal);    
    
    void setClientIpAdress(String clientIpAdress);
    
    static KomorkiOrganizacyjneFacadeLocal create(SecurityContext securityContext, String clientIpAdress) throws NamingException {
        return KomorkiOrganizacyjneFacadeLocal.create(securityContext != null
                ? securityContext.getUserPrincipal() : (Principal) null, clientIpAdress);
    }

    static KomorkiOrganizacyjneFacadeLocal create(Principal principal, String clientIpAdress) throws NamingException {
        InitialContext initialContext = new InitialContext();
        KomorkiOrganizacyjneFacadeLocal bean = (KomorkiOrganizacyjneFacadeLocal) initialContext
                .lookup("java:app/s-portal-ejb/KomorkiOrganizacyjneFacade");
        bean.setPrincipal(principal);
        bean.setClientIpAdress(clientIpAdress);
        return bean;
    }  
    
}
